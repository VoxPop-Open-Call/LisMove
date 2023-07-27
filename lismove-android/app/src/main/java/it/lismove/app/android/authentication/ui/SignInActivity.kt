package it.lismove.app.android.authentication.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivitySignInBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import org.koin.android.ext.android.inject
import timber.log.Timber


class SignInActivity : LisMoveBaseActivity(), LceView<Boolean>{
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private val viewModel: SignInViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignInClient()
        setupFacebookSignUp()

        with(binding){
            emailLoginButton.setOnClickListener { goToEmailSignIn() }
            googleSignInButton.setOnClickListener { googleSignUp() }
            facebookLoginButton.setOnClickListener { facebookSignUp()  }
            backImageView.setOnClickListener { finish() }
            facebookLoginButton.isVisible = true
        }

        viewModel.state.observe(this, LceDispatcher(this))

    }

    private fun facebookSignUp(){
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            listOf("user_photos", "email", "user_birthday", "public_profile")
        )
    }
    private fun setupFacebookSignUp(){
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    signInIfEmailGranted(loginResult)
                }

                override fun onCancel() {}
                override fun onError(exception: FacebookException) {
                    showError(exception.localizedMessage ?: "Si è verificato un errore", binding.root)
                }
            }
        )
    }

    fun signInIfEmailGranted(loginResult:LoginResult?){
        val permissions = AccessToken.getCurrentAccessToken()!!.permissions
        if (permissions.contains("email")) {
            Timber.d("Facebook email ok")
            viewModel.signUpWithFacebook(loginResult?.accessToken)
        } else {
            val request = GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions", null, HttpMethod.DELETE,
                object :  GraphRequest.Callback {
                    override fun onCompleted(response: GraphResponse) {
                        LoginManager.getInstance().logOut()
                        Toast.makeText(
                            this@SignInActivity,
                            "Permesso email negato, riprovare.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
            request.executeAsync()
        }
    }
    private fun setupGoogleSignInClient(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun goToEmailSignIn(){
        val intent = Intent(this, EmailSignInActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun googleSignUp() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
           viewModel.signUpWithGoogleIntent(data)
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLoading() {
        showLoadingAlert()
    }

    override fun onSuccess(data: Boolean) {
    hideLoadingAlert()
        if (data){
            startActivity(Intent(this, SplashScreenActivity::class.java))
            finish()
        }
    }

    override fun onError(throwable: Throwable) {
        hideLoadingAlert()
        showError(throwable.localizedMessage, binding.root)

    }
}