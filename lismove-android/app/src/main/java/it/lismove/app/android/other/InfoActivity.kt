package it.lismove.app.android.other

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.databinding.ActivityInfoBinding
import it.lismove.app.android.general.activity.WebPageActivity
import it.lismove.app.common.ShareUtils

class InfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityInfoBinding
    private val TERMS_AND_CONDITION_URL = "https://lismoveadmin.it/termini-condizioni-lis-move/"
    private val PRIVACY_POLICY_URL = "https://lismoveadmin.it/lis-move-privacy-policy/"
    private val CREDITS_URL = "https://lismoveadmin.it/credits/"
    private val MAIL_URL = "support@lismove.it"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        with(binding){
            termsAndConditionsMenuItem.setOnClickListener { openWebView(TERMS_AND_CONDITION_URL, "Termini e condizioni") }
            privacyPolicyMenuItem.setOnClickListener { openWebView(PRIVACY_POLICY_URL, "Privacy Policy") }
            creditsMenuItem.setOnClickListener { openWebView(CREDITS_URL, "Crediti") }
            mailMenuItem.setOnClickListener { sendMail() }
        }
    }

    private fun sendMail() {
      ShareUtils.saveAttachmentAndOpenShareIntent("Supporto Lis Move", MAIL_URL, activity = this )

    }

    private fun openWebView(url: String, title: String){
        startActivity(WebPageActivity.createIntent(url, this, title))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}