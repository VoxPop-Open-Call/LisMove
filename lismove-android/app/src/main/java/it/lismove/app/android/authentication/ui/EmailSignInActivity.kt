package it.lismove.app.android.authentication.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import it.lismove.app.android.authentication.ui.data.*
import it.lismove.app.android.databinding.ActivityEmailSignInBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

class EmailSignInActivity : LisMoveBaseActivity(){
    lateinit var binding: ActivityEmailSignInBinding
    val viewModel: EmailSignInViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            backArrowImage.setOnClickListener { finish() }
            signInButton.setOnClickListener { signIn() }
            emailTextField.textInputLayout.editText?.inputType =  InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            passwodTextField.textInputLayout.editText?.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            signiInResetPassword.setOnClickListener { resetPassword() }
        }

        viewModel.state.observe(this, {
            clearUI()
            when(it){
                is EmailSignInState.InitialState -> {}
                is EmailSignInState.InputFieldError -> showInputError(it)
                is EmailSignInState.GenericError -> onError(it.genericError!!)
                is EmailSignInState.Loading -> onLoading()
                is EmailSignInState.LoginSuccess -> onSuccess(it)
                is EmailSignInState.ChangePasswordRequired -> goToResetPassword()
            }
        })
    }

    fun resetPassword(){
        Timber.d("Reset Password")
        startActivity(Intent(this, ResetPasswordActivity::class.java ))
    }

    private fun onLoading() {
        Timber.d("onLoading")
        //hideLoadingAlert()
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun onSuccess(data: EmailSignInState) {
        Timber.d("onSuccess $data)")
        //showLoadingAlert()
        binding.progressBar.visibility = View.GONE
        goToSplashScreen()
    }

    private fun onError(message: String) {
        Timber.d("onError $message")
        //hideLoadingAlert()
        binding.progressBar.visibility = View.GONE
        showError(message, binding.root)
    }

    private fun clearUI(){
        binding.emailTextField.clearError()
        binding.passwodTextField.clearError()
    }

    private fun showInputError(state: EmailSignInState){
        state.emailError?.let {  binding.emailTextField.raiseError(it)}
        state.passwordError?.let { binding.passwodTextField.raiseError(it) }
    }

    private fun signIn(){
        viewModel.checkInputAndSignIn(binding.emailTextField.getText(), binding.passwodTextField.getText())
    }

    private fun goToSplashScreen(){
        Timber.d("goToSplashScreen")
        startActivity(Intent(this, SplashScreenActivity::class.java))
        finish()

    }

    private fun goToResetPassword(){
        Timber.d("goToResetPassword")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reimposta password")
        builder.setMessage("Per motivi di sicurezza è necessario reimpostare la propria password")
        builder.setPositiveButton("Reimposta") { dialog, which ->
            viewModel.resetPassword()
            finish()
        }
        builder.setNegativeButton("Annulla") { dialog, which -> }
        builder.show()
    }


}