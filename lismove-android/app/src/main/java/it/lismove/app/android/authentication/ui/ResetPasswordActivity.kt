package it.lismove.app.android.authentication.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.databinding.ActivityEmailSignInBinding
import it.lismove.app.android.databinding.ActivityResetPasswordBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class ResetPasswordActivity : LisMoveBaseActivity() {
    lateinit var binding: ActivityResetPasswordBinding
    val viewModel: ResetPasswordViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            backArrowImage.setOnClickListener { finish() }
            resetButton.setOnClickListener { requestResetPassword() }
            resetButton.isEnabled = false
            resetButton.alpha = 0.5f
            emailTextField.textInputLayout.editText?.inputType =  InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailTextField.editText.addTextChangedListener {  checkEmail() }
        }

    }

    fun requestResetPassword(){
        val email = binding.emailTextField.getText()
        viewModel.sendResetPasswordRequest(email).observe(this){
            dispatchState(it)
        }
    }

    fun dispatchState(state: ResetPasswordState){
        when(state){
            Loading -> showLoadingAlert()
            EmailNotFound -> showError("Nessun utente trovato con la mail indicata", binding.root)
            is Error -> showError(state.message, binding.root)
            ResetRequested -> showConfirmationAlert()
        }
    }

    fun showConfirmationAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Ti abbiamo inviato una mail con una password temporanea")

        builder.setPositiveButton("OK") { dialog, which ->
           finish()
        }

        builder.show()
    }

    private fun checkEmail(){
        val email = binding.emailTextField.getText()

        if(!viewModel.isEmailValid(email)){
            binding.emailTextField.raiseError("Inserisci una email valida")
            binding.resetButton.isEnabled = false
            binding.resetButton.alpha = 0.8f

        }else{
            binding.emailTextField.clearError()
            binding.resetButton.isEnabled = true
            binding.resetButton.alpha = 1f

        }
    }


}