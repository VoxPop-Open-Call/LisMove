package it.lismove.app.android.authentication.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.data.SignUpState
import it.lismove.app.android.databinding.ActivitySignUpBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import org.koin.android.ext.android.inject
import timber.log.Timber


class SignUpActivity : LisMoveBaseActivity(), LceView<SignUpState>{
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    private val viewModel: SignUpViewModel by inject()
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignUp()
        setupFacebookSignUp()

        with(binding){
            emailSignUpButton.setOnClickListener { signUp()}
            backArrowImage.setOnClickListener { finish() }
            emailInputLayout.textInputLayout.editText?.inputType =   InputType.TYPE_CLASS_TEXT or  InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailInputLayout.textInputLayout.editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
            passwordInputLayout.textInputLayout.editText?.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            confirmPasswordLayout.textInputLayout.editText?.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            emailInputLayout.editText.addTextChangedListener {  checkEmail() }
            passwordInputLayout.editText.addTextChangedListener { checkPassword() }
            confirmPasswordLayout.editText.addTextChangedListener { checkConfirmPassword() }

            signUpButtonGoogle.setOnClickListener { googleSignUp() }

            signUpFacebook.setOnClickListener {
                LoginManager.getInstance().logOut()
                LoginManager.getInstance().logInWithReadPermissions(
                    this@SignUpActivity,
                        listOf("email")
            ) }


        }
        viewModel.state.observe(this, LceDispatcher(this))
    }

    private fun signUp(){
        val email = binding.emailInputLayout.getText()
        val password = binding.passwordInputLayout.getText()
        val confirmPassword = binding.confirmPasswordLayout.getText()

        viewModel.checkInputAndSignIn(email, password, confirmPassword)
    }

    private fun checkInputOrSeeError(){
        checkEmail()
        checkPassword()
        checkConfirmPassword()
    }

    private fun checkEmail(){
        val email = binding.emailInputLayout.getText()
        if(!viewModel.isEmailValid(email)) binding.emailInputLayout.raiseError("Inserisci una email valida")
        else binding.emailInputLayout.clearError()
    }
    private fun checkPassword(){
        val password = binding.passwordInputLayout.getText()
        if(!viewModel.isPasswordValid(password)) binding.passwordInputLayout.raiseError("La passwrod deve contenere almeno 6 caratteri")
        else binding.passwordInputLayout.clearError()

    }
    private fun checkConfirmPassword(){
        val password = binding.passwordInputLayout.getText()
        val confirmPassword = binding.confirmPasswordLayout.getText()
        if(!viewModel.arePasswordTheSame(password, confirmPassword)) binding.confirmPasswordLayout.raiseError(
                "Le password non coincidono"
        )
        else binding.confirmPasswordLayout.clearError()

    }

    override fun onLoading() {
        Timber.d("onLoading")
        showLoadingAlert()
        //binding.progressBar.visibility = View.VISIBLE
    }

    override fun onSuccess(data: SignUpState) {
        hideLoadingAlert()
        //binding.progressBar.visibility = View.INVISIBLE
        if(data.signedUp){
            goToSplashScreen()
        }else if(data.inputError){
            checkInputOrSeeError()
        }
        Timber.d("onSuccess")
    }

    private fun goToSplashScreen() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        finish()
    }

    override fun onError(throwable: Throwable) {
       // binding.progressBar.visibility = View.INVISIBLE
        hideLoadingAlert()
        dismissKeyboard()
        showError("onError ${throwable.localizedMessage}", binding.root)
        Timber.d("onError ${throwable.localizedMessage}")
    }

    private fun googleSignUp() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        googleSignInClient.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:" + account.id)
                viewModel.signUpWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.d("SignIn failed")

            }
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setupGoogleSignUp(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
            viewModel.signUpWithFacebook(loginResult?.accessToken)
        } else {
            val request = GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions", null, HttpMethod.DELETE,
                object :  GraphRequest.Callback {
                    override fun onCompleted(response: GraphResponse) {
                        LoginManager.getInstance().logOut()
                        Toast.makeText(
                            this@SignUpActivity,
                            "Permesso email negato, riprovare.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
            request.executeAsync()
        }
    }

}