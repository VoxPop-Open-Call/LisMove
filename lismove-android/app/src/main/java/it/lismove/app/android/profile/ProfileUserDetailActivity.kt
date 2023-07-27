package it.lismove.app.android.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.lismove.app.android.R

class ProfileUserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}