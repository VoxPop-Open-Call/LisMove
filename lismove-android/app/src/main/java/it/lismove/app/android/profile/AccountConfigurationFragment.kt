package it.lismove.app.android.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.ui.AccountConfigurationViewModel
import it.lismove.app.android.authentication.ui.SplashScreenActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.RegistrationCodeActivity
import it.lismove.app.android.initiative.ui.data.WorkAddress
import org.koin.android.ext.android.inject
import timber.log.Timber

class AccountConfigurationFragment: BaseProfileDetailFragment(), LceView<LisMoveUser> {
    val viewModel: AccountConfigurationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("oncreate")
        bindUserProfile(viewModel.user)
        return view
    }

    override fun updateAccount(
        name: String,
        surname: String,
        nickname: String,
        bornDate: String,
        gender: String,
        phoneNumber: String,
        iban: String
    ) {
        viewModel.updateProfile(name, surname, nickname,  bornDate, gender, iban)
                .observe(this,
                        LceDispatcher(this))
    }

    override fun getCityFromIntent(intent: Intent?): LisMoveCityEntity? {
        return viewModel.getCityFormIntent(intent)
    }

    override fun getUserAddress(): WorkAddress {
        return viewModel.homeAddress
    }

    override fun setUserAddress(workAddress: WorkAddress) {
        viewModel.homeAddress = workAddress
    }

    override fun onLoading() {
        showLoadingAlert()
    }

    override fun onSuccess(data: LisMoveUser) {
        hideLoadingAlert()
        goToNextScreen()
    }

    private fun goToNextScreen(){
        goToAddInitiativeScreen()
    }

    private fun goToSplashScreen(){
        startActivity(Intent(requireActivity(), SplashScreenActivity::class.java))
        requireActivity().finish()

    }

    fun goToAddInitiativeScreen(){
        activity?.let {
            startActivity(Intent(RegistrationCodeActivity.getIntent(it, true)))
            requireActivity().finish()
        }
    }
    override fun onError(throwable: Throwable) {
        super.onError(throwable)

    }
}