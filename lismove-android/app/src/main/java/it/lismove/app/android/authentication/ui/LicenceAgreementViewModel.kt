package it.lismove.app.android.authentication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.LisMoveAppSettings
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent

class LicenceAgreementViewModel(
    private val userRepository: UserRepository,
    private val tempPrefsRepository: TempPrefsRepository,
    private val user: LisMoveUser,
) : ViewModel(), KoinComponent {
    val appSetting = LisMoveAppSettings
    fun savePreferences(marketingTermsAccepted: Boolean): LiveData<Lce<Boolean>> = flow{
        emit(LceLoading())
        val updatedUser = user.copy(marketingTermsAccepted = marketingTermsAccepted, termsAccepted = true)
        userRepository.updateUserProfile(updatedUser)
        tempPrefsRepository.saveTempUser(updatedUser)
        emit(LceSuccess(true))
    }.catch { emit(LceError(it)) }.asLiveData()

}