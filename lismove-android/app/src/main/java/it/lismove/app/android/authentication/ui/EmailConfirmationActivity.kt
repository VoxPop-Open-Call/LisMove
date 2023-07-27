package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.databinding.ActivityEmailConfirmationBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class EmailConfirmationActivity : AppCompatActivity() {
    val viewModel: EmailConfirmationViewModel by inject()
    lateinit var binding: ActivityEmailConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){

            emailConfirmationMessage.text = viewModel.getUserMailOrError(this@EmailConfirmationActivity)
            emailConfirmationGoBack.setOnClickListener {
                viewModel.signOut()
                GlobalScope.launch {
                    delay(2000)
                    startActivity(Intent(this@EmailConfirmationActivity, SplashScreenActivity::class.java))
                    finish()
                }
            }
            sendEmail.setOnClickListener { viewModel.sendEmailAgain() }
        }

        viewModel.getUserState().observe(this,{
            if(it){
                startActivity(Intent(this, SplashScreenActivity::class.java))
                finish()
            }
        } )


    }
}