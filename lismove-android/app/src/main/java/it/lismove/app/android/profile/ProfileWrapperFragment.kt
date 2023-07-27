package it.lismove.app.android.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.SplashScreenActivity
import it.lismove.app.android.awards.AwardWrapperActivity
import it.lismove.app.android.car.ui.CarConfigurationActivity
import it.lismove.app.android.databinding.FragmentProfileWrapperBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.session.ui.SessionsHistoryActivity
import it.lismove.app.android.settings.SensorDetailActivity
import org.koin.android.ext.android.inject

class ProfileWrapperFragment : LisMoveFragment(R.layout.fragment_profile_wrapper){
    lateinit var binding: FragmentProfileWrapperBinding
    val viewModel: ProfileWrapperViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileWrapperBinding.bind(view)

        viewModel.errorObservable.observe(this) {
            showAlertDialog(null, it, {})}
        viewModel.eventObservable.observe(this){dispatchEvent(it)}
        with(binding){

            profileDetailMenuItem.setOnClickListener {
                startActivity(Intent(requireActivity(), ProfileUserDetailActivity::class.java))
            }
            profileHistoryMySession.setOnClickListener { openMySessionHistory() }
            profileHistoryHomeWorkSession.setOnClickListener { openMyHomeWorkSessionHistory() }
            profileMyAwards.setOnClickListener { openMyAward() }

            logoutOutButton.setOnClickListener { logout() }

        }
    }

    private fun dispatchEvent(it: EVENT) {
        when(it){
            EVENT.HIDE_LOADING -> hideLoadingAlert()
            EVENT.SHOW_LOADING -> showLoadingAlert()
            EVENT.LOGOUT -> goToSplashScreen()
        }
    }

    private fun goToSplashScreen() {
        val intent = Intent(requireActivity(), SplashScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun logout(){
        viewModel.logout()
    }

    fun openMySessionHistory(){
        startActivity(SessionsHistoryActivity.getIntent(requireActivity(), false))
    }
    fun openMyHomeWorkSessionHistory(){
        startActivity(SessionsHistoryActivity.getIntent(requireActivity(), true))
    }
    fun openMyAward(){
        startActivity(AwardWrapperActivity.getMyAwardOnlyActivity(requireActivity()))
    }

}