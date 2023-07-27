package it.lismove.app.android.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.ui.SplashScreenActivity
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.android.initiative.ui.parser.getHomeAddress
import org.koin.android.ext.android.inject
import timber.log.Timber


class ProfileUserDetailFragment : BaseProfileDetailFragment()  {
    var editEnabled = false
    val viewModel: ProfileUserDetailViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)
        Timber.d("onCreateView")

        _binding?.let {
            it.logoutOutButton.visibility = View.GONE
            it.saveUserConfigButton.text = "Modifica"
            it.saveUserConfigButton.setOnClickListener {
                if(editEnabled){
                    confirmAccountConfig()
                }else{
                    viewModel.setModeEditing(true)
                }
            }
            viewModel.stateObservable.observe(viewLifecycleOwner){
                dispatchEvents(it)
            }
        }
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
        viewModel.updateProfile(name, surname, nickname, bornDate, gender, phoneNumber, iban)

    }

    override fun getCityFromIntent(intent: Intent?): LisMoveCityEntity? {
        Timber.d("getCityFromIntent")
        return viewModel.getCityFormIntent(intent)
    }


    fun dispatchEvents(state: ProfileUserDetailState){
        if(state != ProfileUserDetailLoading) hideLoadingAlert()
        Timber.d("dispatchEvents")
        when(state){
            is ProfileUserDetailUserNotEditing -> bindUserProfile(state.user, false)
            is ProfileUserDetailUserUpdated ->  onEditingCompleted()
            is ProfileUserDetailUserEditing -> bindUserProfile(state.user, true)
            ProfileUserDetailLogout -> goToSplash()
            ProfileUserDetailLoading -> showLoadingAlert()
            is ProfileUserDetailError -> onError(state.error)
        }
    }
    override fun getUserAddress(): WorkAddress {
        return viewModel.updatedUser.getHomeAddress()
    }

    private fun onEditingCompleted(){
        activity?.finish()
    }
    override fun setUserAddress(workAddress: WorkAddress) {
        viewModel.setHomeAddress(workAddress)
    }

     override fun onError(throwable: Throwable) {
         super.onError(throwable)
         Timber.d("onError")
         hideLoadingAlert()
         showError(throwable.localizedMessage ?: "Si è verificato un problema")
    }

    fun setEditingEnabled(enabled: Boolean){
        editEnabled = enabled
        _binding?.saveUserConfigButton?.text = if (editEnabled) "Salva" else "Modifica"
        _binding?.let {
            it.nameTextField.isEnabled = enabled
            it.surnameTextField.isEnabled = enabled
            it.nickNameTextFIeld.isEnabled = enabled
            it.bornDateTextField.isEnabled = enabled
            it.genderTextField.isEnabled = enabled
            it.phoneNumberTextField.isEnabled = enabled
            it.homeAddressTextField.isEnabled = enabled
            it.ibanTextField.isEnabled = enabled
            if(enabled){
                activity?.let {
                    (it as? AppCompatActivity)?.let {
                        phoneNumberKit.setupCountryPicker(it)
                    }
                }
            }else{
                it.phoneNumberLayout.setStartIconOnClickListener {  }
            }
        }
    }


    private fun bindUserProfile(user: LisMoveUser, editingEnabled: Boolean) {
        Timber.d("bindUserProfile $editingEnabled")
        setEditingEnabled(editingEnabled)
        bindUserProfile(user)

    }

    private fun goToSplash(){
        hideLoadingAlert()
        startActivity(Intent(requireActivity(), SplashScreenActivity::class.java))
        requireActivity().finish()
    }



}