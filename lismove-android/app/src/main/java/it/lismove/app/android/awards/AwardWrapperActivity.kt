package it.lismove.app.android.awards

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAwardWrapperBinding

class AwardWrapperActivity : AppCompatActivity() {
    lateinit var binding: ActivityAwardWrapperBinding
    companion object{
        val TYPE = "AWARD_WRAPPER_TYPE"
        val TYPE_MY_AWARD = "AWARD_WRAPPER_TYPE_MY_AWARDS"

        fun getMyAwardOnlyActivity(ctx: Context): Intent{
            return Intent(ctx, AwardWrapperActivity::class.java).apply {
                putExtra(TYPE, TYPE_MY_AWARD)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwardWrapperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}