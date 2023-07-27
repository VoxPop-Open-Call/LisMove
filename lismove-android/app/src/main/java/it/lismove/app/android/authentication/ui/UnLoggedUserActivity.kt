package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.databinding.ActivityUnLoggedUserBinding

class UnLoggedUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnLoggedUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnLoggedUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            signInButton.setOnClickListener { goToSignIn() }
            signUpButton.setOnClickListener { goToSignUp() }
        }

    }

    private fun goToSignIn(){
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goToSignUp(){
        startActivity(Intent(this, SignUpActivity::class.java))

    }

}