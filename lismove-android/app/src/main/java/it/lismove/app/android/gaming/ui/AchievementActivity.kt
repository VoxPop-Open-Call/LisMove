package it.lismove.app.android.gaming.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAchievementBinding

class AchievementActivity : AppCompatActivity() {
    lateinit var binding: ActivityAchievementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}