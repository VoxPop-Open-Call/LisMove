package it.lismove.app.android.authentication.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityEmailSignInBinding
import it.lismove.app.android.databinding.ActivitySignInBinding
import org.koin.android.ext.android.inject

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmailSignInBinding
    val viewModel: ChangePasswordViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val email = intent.getStringExtra(EXTRA_EMAIL) ?: "test@test.it"

        with(binding){
            activityTitle.text = "Reimposta password"
            emailTextField.editText.setText(email)
            emailTextField.editText.isEnabled = false
            activityDescription.visibility = View.VISIBLE
            signInButton.text = "Reimposta password"
        }
    }

    companion object {
        const val  EXTRA_EMAIL = "extra_email"
        fun getIntent(context: Context, email: String): Intent {
            return Intent(context, ChangePasswordActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
            }
        }
    }
}