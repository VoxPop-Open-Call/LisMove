package it.lismove.app.android.gaming.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import coil.load
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAchievementDetailBinding
import it.lismove.app.android.gaming.apiService.data.Achievement

class AchievementDetailActivity : AppCompatActivity() {
    companion object{
        const val INTENT_ACHIEVEMENT = "INTENT_ACHIEVEMENT"
        fun getIntent(ctx: Context, achievement: Achievement): Intent{
            return Intent(ctx, AchievementDetailActivity::class.java).apply {
                putExtra(INTENT_ACHIEVEMENT, Gson().toJson(achievement))
            }
        }
    }
    lateinit var binding: ActivityAchievementDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAchievementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupUI()
    }

    fun setupUI(){
        val detail = intent.getStringExtra(INTENT_ACHIEVEMENT)?.let {
            val detail = Gson().fromJson(it, Achievement::class.java)
            with(binding){
                with(binding){
                    achievementTitle.text = detail.name

                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.prize_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}