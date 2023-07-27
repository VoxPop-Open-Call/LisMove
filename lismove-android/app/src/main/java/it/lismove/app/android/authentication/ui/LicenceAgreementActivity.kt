package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityLicenceAgreementBinding
import it.lismove.app.android.databinding.ActivitySignInBinding
import it.lismove.app.android.general.activity.WebPageActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import org.koin.android.ext.android.inject

class LicenceAgreementActivity : AppCompatActivity(), LceView<Boolean> {

    val viewModel: LicenceAgreementViewModel by inject()
    lateinit var binding: ActivityLicenceAgreementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenceAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            licenceNextButton.isEnabled = false
            licenceNextButton.setOnClickListener { goToAccountConfiguration() }
            termsOfUseCheckbox.setOnClickListener { checkRequiredBoxChecked() }
            privacyPolicyCheckBox.setOnClickListener { checkRequiredBoxChecked() }
            ageCheckBox.setOnClickListener { checkRequiredBoxChecked() }
            termsOfUseTextView.setOnClickListener { openWebPage(viewModel.appSetting.termsOfService) }
            privacyPolicyTextView.setOnClickListener { openWebPage(viewModel.appSetting.privacyPolicy) }
        }
    }

    private fun goToAccountConfiguration(){
        viewModel.savePreferences(binding.marketingCheckbox.isChecked).observe(this, LceDispatcher(this))
    }
    private fun openWebPage(url: String){
        startActivity(
            WebPageActivity.createIntent(url, this)
        )
    }

    private fun checkRequiredBoxChecked(){
        with(binding){
            val required = termsOfUseCheckbox.isChecked && privacyPolicyCheckBox.isChecked && ageCheckBox.isChecked
            licenceNextButton.isEnabled = required
        }
    }

    override fun onLoading() {

    }

    override fun onSuccess(data: Boolean) {
        startActivity(Intent(this, AccountConfigurationActivity::class.java))
        finish()
    }

    override fun onError(throwable: Throwable) {
        Toast.makeText(this, throwable.localizedMessage ?: "Si è verificato un problema", Toast.LENGTH_SHORT).show()
    }

}