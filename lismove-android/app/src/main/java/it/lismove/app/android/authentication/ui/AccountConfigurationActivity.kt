package it.lismove.app.android.authentication.ui

import android.os.Bundle
import it.lismove.app.android.databinding.ActivityAccountConfigurationBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity


class AccountConfigurationActivity : LisMoveBaseActivity(){
    lateinit var binding: ActivityAccountConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountConfigurationBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

}