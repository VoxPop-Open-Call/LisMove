package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.gson.Gson
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.useCases.EditProfileUseCase
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.android.initiative.ui.parser.getHomeAddress
import kotlinx.coroutines.flow.catch
import timber.log.Timber

class AccountConfigurationViewModel(
        private val authRepository: AuthRepository,
        private val editProfileUseCase: EditProfileUseCase,
        var user: LisMoveUser
): ViewModel() {

    var city: LisMoveCityEntity? = null
    var homeAddress: WorkAddress = user.getHomeAddress()

    init {
        precompileField()
    }

    private fun precompileField(){
        val authUser = authRepository.getCurrentAuthUser()
        val displayName = authUser?.displayName

        if(user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()){
            user.firstName = displayName?.substringBeforeLast(" ")
            user.lastName = displayName?.substringAfterLast(" ")
        }

        city = user.homeCityExtended
        if(user.avatarUrl.isNullOrEmpty()){
            if(authUser?.photoUrl?.authority != "graph.facebook.com"){
                user.avatarUrl = authUser?.photoUrl?.toString()
            }
        }
    }

    fun updateProfile(
        name: String,
        surname: String,
        nickname: String,
        bornDate: String,
        gender: String,
        iban: String
    ): LiveData<Lce<LisMoveUser>> {

        val bornDateInMillis = DateTimeUtils.getTimeStampFromDateFormatted(bornDate)
        val updatedUser = user.copy(firstName = name, lastName = surname, username = nickname,
                homeCity = homeAddress.city,
                gender = gender, birthDate = bornDateInMillis,
                emailVerified = true, signupCompleted = true, homeAddress = homeAddress.address,
            homeNumber = homeAddress.number, homeLatitude = homeAddress.lat,
            homeLongitude = homeAddress.lng,
            homeCityExtended = homeAddress.cityExtended, iban = iban)
        return editProfileUseCase.updateProfile(updatedUser).catch { emit(LceError(it)) }.asLiveData()
    }


    fun getCityFormIntent(intent: Intent?): LisMoveCityEntity?{
        val cityString = intent?.extras?.getString(CityPickerActivity.EXTRA_CITY) ?: ""
        Timber.d("city $cityString")
        city = Gson().fromJson(cityString, LisMoveCityEntity::class.java)
        return  city
    }


}
