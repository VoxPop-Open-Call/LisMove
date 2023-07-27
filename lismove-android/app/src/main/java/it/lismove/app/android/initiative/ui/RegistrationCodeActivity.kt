package it.lismove.app.android.initiative.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import cn.pedant.SweetAlert.SweetAlertDialog
import it.lismove.app.android.authentication.ui.SplashScreenActivity
import it.lismove.app.android.databinding.ActivityRegistrationCodeBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.general.utils.dismissKeyboard
import it.lismove.app.android.initiative.ui.data.CodeEmptyError
import it.lismove.app.android.initiative.ui.data.CodeIncorrectError
import it.lismove.app.android.initiative.ui.data.EnrollmentFinished
import it.lismove.app.room.entity.EnrollmentEntity
import org.koin.android.ext.android.inject
import timber.log.Timber

class RegistrationCodeActivity : LisMoveBaseActivity(), LceView<EnrollmentEntity> {
    lateinit var binding: ActivityRegistrationCodeBinding
    var dialog: SweetAlertDialog? = null
    val viewModel: RegistrationCodeViewModel by inject()
    var previousRequest: LiveData<Lce<EnrollmentEntity>>? = null

    companion object{
        internal const val INTENT_IS_FROM_REGISTRATION = "INTENT_IS_FROM_REGISTRATION"

        fun getIntent(ctx: Context, isFromRegistration: Boolean = false): Intent{
            return Intent(ctx, RegistrationCodeActivity::class.java).apply {
                putExtra(INTENT_IS_FROM_REGISTRATION, isFromRegistration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = SweetAlertDialog(this)
        viewModel.setupFromIntent(intent)
        with(binding){
            showBackIfNotFromRegistration()
            registrationCodeLayout.errorIconDrawable = null
            registrationCodeVerifyButton.setOnClickListener { sendRequest()  }
            registrationCodeSkip.isVisible = viewModel.isFromRegistration
            registrationCodeEditText.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_SEND ){
                    Timber.d("Done clicked")
                    v.dismissKeyboard()
                    sendRequest()
                }
                return@setOnEditorActionListener true
            }
            registrationCodeSkip.setOnClickListener { goToSplashScreen() }
        }
    }

    private fun showBackIfNotFromRegistration() {
        supportActionBar?.setDisplayHomeAsUpEnabled(viewModel.isFromRegistration.not())
    }

    private fun goToSplashScreen(){
        startActivity(Intent(this, SplashScreenActivity::class.java))
        finish()
    }

    private fun sendRequest(){
        clearEmptyCodeError()
        previousRequest?.removeObservers(this)
        val insertedCode = binding.registrationCodeEditText.text.toString()
        previousRequest = viewModel.validateCode(insertedCode)
        previousRequest?.observe(this, LceDispatcher(this))
    }

    private fun showAlertDialogError(message: String){
        dialog?.apply{
            setTitleText(message)
            changeAlertType(SweetAlertDialog.ERROR_TYPE)
            show()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onCodeEmpty(message: String){
        binding.registrationCodeLayout.error = message
    }

    private fun clearEmptyCodeError(){
        binding.registrationCodeEditText.error = null

    }

    private fun goToNextActivity(enrollment: EnrollmentEntity){
        startActivity(InitiativeConfigurationActivity.getIntent(
            this,
            enrollment,
            true,
            viewModel.isFromRegistration
        ))
        finish()
    }

    override fun onLoading() {
        Timber.d("OnLoading")
        dialog = SweetAlertDialog(this).apply {
            titleText = "Caricamento"
            changeAlertType(SweetAlertDialog.PROGRESS_TYPE)
            show()
        }
    }

    override fun onSuccess(data: EnrollmentEntity) {
       // dialog.dismissWithAnimation()
        dialog?.apply{
            titleText = "Congratulazioni"
            contentText = "Codice iniziativa valido"
            confirmText = "Continua"
            setConfirmClickListener {
                it.dismissWithAnimation()
                goToNextActivity(data)
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        }
    }

    override fun onError(throwable: Throwable) {
        when(throwable){
            is CodeEmptyError -> onCodeEmpty(throwable.message)
            is CodeIncorrectError -> showAlertDialogError(throwable.message)
            is EnrollmentFinished -> showAlertDialogError(throwable.message)
            else -> {
                dialog?.dismissWithAnimation()
                showError(throwable.localizedMessage, this.binding.root)
            }
        }
    }

    override fun onBackPressed() {
        if(viewModel.isFromRegistration){
            goToSplashScreen()
        }else{
            super.onBackPressed()
        }
    }
}