package it.lismove.app.android.authentication.useCases.impl

import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.useCases.EditProfileUseCase
import it.lismove.app.android.authentication.useCases.data.DataIncompleteError
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.regex.Pattern

class EditProfileUseCaseImpl(
        val userRepository: UserRepository,
        val tempPrefsRepository: TempPrefsRepository,
): EditProfileUseCase, KoinComponent {

    val nameError = "Inserisci il tuo nome"
    var surnameError = "Inserisci il tuo cognome"
    var nickNameEmptyError = "Inserisci un nickname senza spazie o caratteri speciali"
    var noAddressError = "Inserisci un indirizzo"
    var noNumberError = "Inserisci il numero civico"
    var noCityError = "Inserisci il comune"
    var nickNameAlreadyUsedError = "Nickname non disponibile"
    var noBirthDateError = "Inserisci la tua data di nascita"
    var targetLocationError = "Seleziona un comune"
    val nickNamePattern = Pattern.compile("^[a-zA-Z0-9_-]+\$")

    override fun updateProfile(user: LisMoveUser): Flow<Lce<LisMoveUser>> = flow<Lce<LisMoveUser>>{
        emit(LceLoading())
        delay(1000)
        if(isInputCorrect(user)){
            user.homeAddress = if(user.homeAddress.isNullOrEmpty()) null else user.homeAddress
            user.homeNumber = if(user.homeNumber.isNullOrEmpty()) null else user.homeNumber
            val updatedUser = userRepository.updateUserProfile(user)

            tempPrefsRepository.saveTempUser(updatedUser)
            emit(LceSuccess(updatedUser))
        }else{
            val nameError = if(user.firstName.isNullOrEmpty()) nameError else ""
            val surnameError = if(user.lastName.isNullOrEmpty()) surnameError else ""
            val nicknameError = if(user.username.isNullOrEmpty() || !nickNamePattern.matcher(user.username).matches()) nickNameEmptyError else ""
            val addressError = if(!isAddressValid(user) && user.homeAddress.isNullOrEmpty()) noAddressError else ""
            val numberError = if(!isAddressValid(user) && user.homeNumber.isNullOrEmpty()) noNumberError else ""
            val cityError = if(!isAddressValid(user) && user.homeCity == null) noCityError else ""
            val dateError = if(user.birthDate == null) noBirthDateError else ""
            val ibanError = if(user.isIbanNullOrCorrect()) "" else "IBAN non valido"
            emit(LceError(DataIncompleteError(nameError, surnameError, nicknameError, addressError, numberError, cityError,  targetLocationError, dateError, ibanError)))
        }
    }



    private fun isInputCorrect(user: LisMoveUser): Boolean{
        with(user){
            return !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() &&
                    !username.isNullOrEmpty()  &&
                    birthDate != null && nickNamePattern.matcher(username).matches() && isAddressValid(user)
        }
    }


    fun isAddressValid(user: LisMoveUser): Boolean{
        val hasNone = user.homeAddress.isNullOrEmpty().and(user.homeCity == null)
        val hasAll = (!user.homeAddress.isNullOrEmpty()).and(user.homeCity != null)
        Timber.d("IsUserAddressValid ${hasAll || hasNone}")
        return hasAll || hasNone
    }
}