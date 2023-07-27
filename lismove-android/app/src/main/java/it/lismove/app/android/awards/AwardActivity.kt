package it.lismove.app.android.awards

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.databinding.ActivityAwardBinding
import org.koin.android.ext.android.inject

class AwardActivity : AppCompatActivity() {
    companion object{
        val INTENT_CALLER_ID = "INTENT_RANKING_ID"
        val INTENT_TITLE = "INTENT_TITLE"
        val INTENT_SUBTITLE = "INTENT_SUBTITLE"
        val INTENT_TYPE = "INTENT_TYPE"
        val INTENT_TYPE_RANKING = "INTENT_TYPE_RANKING"
        val INTENT_TYPE_ACHIEVEMENT = "INTENT_TYPE_ACHIEVEMENT"

        fun getIntentFromRanking(ctx: Context, rankingId: Long, title: String?, subtitle: String?): Intent {
            return Intent(ctx,AwardActivity::class.java).apply {
                putExtra(INTENT_CALLER_ID, rankingId)
                putExtra(INTENT_TITLE, title)
                putExtra(INTENT_SUBTITLE, subtitle)
                putExtra(INTENT_TYPE, INTENT_TYPE_RANKING)
            }
        }

        fun getIntentFromAchievement(ctx: Context, achievementId: Long, title: String?, subtitle: String?): Intent {
            return Intent(ctx,AwardActivity::class.java).apply {
                putExtra(INTENT_CALLER_ID, achievementId)
                putExtra(INTENT_TITLE, title)
                putExtra(INTENT_SUBTITLE, subtitle)
                putExtra(INTENT_TYPE, INTENT_TYPE_ACHIEVEMENT)
            }
        }
    }
    lateinit var binding: ActivityAwardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAwardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}