package it.lismove.app.android.profile

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.ui.CityPickerActivity
import it.lismove.app.android.authentication.useCases.EditProfileUseCase
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.network.LismoveNetworkException
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.ui.data.WorkAddress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

class ProfileUserDetailViewModel(
    var user: LisMoveUser,
    val authRepository: AuthRepository,
    private val editProfileUseCase: EditProfileUseCase,
) : ViewModel(){
    private val _stateFlow = MutableStateFlow<ProfileUserDetailState>(ProfileUserDetailUserNotEditing(user))
    val stateObservable = _stateFlow.asLiveData()
    var updatedUser = user

    fun setModeEditing(flag: Boolean){
        viewModelScope.launch {
            if(flag){
                _stateFlow.emit(ProfileUserDetailUserEditing(updatedUser))
            }else{
                _stateFlow.emit(ProfileUserDetailUserNotEditing(updatedUser))

            }
        }

    }

    fun setHomeAddress(workAddress: WorkAddress){
        updatedUser.homeAddress = workAddress.address
        updatedUser.homeNumber = workAddress.number
        updatedUser.homeCity = workAddress.city
        updatedUser.homeCityExtended = workAddress.cityExtended
        updatedUser.homeLatitude = workAddress.lat
        updatedUser.homeLongitude = workAddress.lng
    }

    fun updateProfile(
            name: String,
            surname: String,
            nickname: String,
            bornDate: String,
            gender: String,
            phoneNumber: String?,
            iban: String
    ) {
        val bornDateInMillis = DateTimeUtils.getTimeStampFromDateFormatted(bornDate)
        updatedUser =  user.copy(
            firstName = name,
            lastName = surname,
            username = nickname,
            gender = gender,
            birthDate = bornDateInMillis,
            homeCity = updatedUser.homeCityExtended?.id,
            phoneNumber = phoneNumber,
            iban = iban
        )
        Timber.d("EditProfile")
        editProfile(updatedUser)
    }


    fun getCityFormIntent(intent: Intent?): LisMoveCityEntity? {
        Timber.d("getCityFromIntent")
        val cityString = intent?.extras?.getString(CityPickerActivity.EXTRA_CITY)?.let {
            val selectedCity = Gson().fromJson(it, LisMoveCityEntity::class.java)
            updatedUser.homeCityExtended = selectedCity
            updatedUser.homeCity = selectedCity.id
        }
        return  updatedUser.homeCityExtended
    }

    private fun editProfile(updateUser: LisMoveUser) {
        viewModelScope.launch {
            _stateFlow.emitAll(editProfileUseCase.updateProfile(updateUser).map{
                when(it){
                    is LceLoading -> ProfileUserDetailLoading
                    is LceError -> ProfileUserDetailError(it.error)
                    is LceSuccess -> {
                        user = updateUser
                        ProfileUserDetailUserUpdated(it.data)
                    }
                }
            }.catch {
                if(it is LismoveNetworkException){
                    emit(ProfileUserDetailError(it))
                }else if(it is IOException){
                    Timber.d("error connection")
                    updatedUser = user
                    emit(ProfileUserDetailError(Error("Nessuna connessione ad internet, impossibile aggiornare l'utente")))
                    emit(ProfileUserDetailUserNotEditing(updatedUser))
                }else{
                    emit(ProfileUserDetailError(it))
                }
            })
        }
    }


}

sealed class ProfileUserDetailState

object ProfileUserDetailLoading: ProfileUserDetailState()
class ProfileUserDetailUserNotEditing(val user: LisMoveUser): ProfileUserDetailState()
class ProfileUserDetailUserUpdated(val user: LisMoveUser): ProfileUserDetailState()
class ProfileUserDetailUserEditing(val user: LisMoveUser): ProfileUserDetailState()
class ProfileUserDetailError(val error: Throwable): ProfileUserDetailState()
object ProfileUserDetailLogout: ProfileUserDetailState()

