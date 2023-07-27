package it.lismove.app.android.authentication.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.MainActivity
import it.lismove.app.android.R
import it.lismove.app.android.authentication.useCases.data.*
import it.lismove.app.android.databinding.ActivitySplashScreenBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber

class SplashScreenActivity : LisMoveBaseActivity(), LceView<LoginState> {
    private val viewModel: SplashScreenViewModel by inject()
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("Oncreate")

        viewModel.startingIntentExtras = intent.extras
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel.getAuthenticationState().observe(this, LceDispatcher(this))
        viewModel.prepareGpsLocationFix(applicationContext)
    }

    override fun onLoading() {
        Timber.d("OnLoading")
        binding.splashInnerStarImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.clockwise))
    }

    override fun onSuccess(data: LoginState) {
        Timber.d("onSuccess")
        when(data){
            is LoginLoggedSuccess -> goToMainActivity()
            is LoginLoggedProfileIncomplete -> goToAccountConfiguration()
            is LoginUnLogged -> goToUnLoggedUser()
            is LoginLoggedBlocked -> onAccountBlocked()
            is LoginEmailNotVerified -> goToVerifyEmail()
            is LoginTermsNotAccepted -> goToLicenceAgreement()
            is CachedUserExpired -> showAlert(Exception("Modalità offline non disponibile"))
        }
    }

    fun goToLicenceAgreement(){
        Timber.d("goToLicenceAgreement")
        val intent = Intent(this, LicenceAgreementActivity::class.java)
        startActivity(intent)
        finish()
    }


    fun showAlert(throwable: Throwable){
        binding.splashWheelImage.clearAnimation()
        val errorMessage = throwable.localizedMessage ?: "Si è verificato un errore"
        showError(errorMessage, binding.root)
    }

    override fun onError(throwable: Throwable) {
        Timber.e(throwable)
        Timber.d("onError: ${throwable.localizedMessage}")
        binding.splashWheelImage.clearAnimation()
        val errorMessage = throwable.localizedMessage ?: "Si è verificato un errore"
        showError(errorMessage, binding.root)
        viewModel.viewModelScope.launch {
            delay(2000)
            goToUnLoggedUser()
        }
    }

    private fun onAccountBlocked(){
        Timber.d("onAccountBlocked")
        BugsnagUtils.logEvent("onAccountBlocked")
    }

    private fun goToUnLoggedUser(){
        Timber.d("goToUnLoggedUser")
        val intent = Intent(this, UnLoggedUserActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity(){
        Timber.d("goToMainActivity")

        val intent = Intent(this, MainActivity::class.java).apply {
        //val intent = Intent(this, TestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Propagate starting intent extras
            viewModel.startingIntentExtras?.let { this.putExtras(it) }
        }

            
        startActivity(intent)
        finish()
    }

    private fun goToVerifyEmail(){
        Timber.d("goToVerifyEmail")

        val intent = Intent(this, EmailConfirmationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun goToAccountConfiguration(){
        Timber.d("goToAccountConfiguration")
        val intent = Intent(this, AccountConfigurationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}