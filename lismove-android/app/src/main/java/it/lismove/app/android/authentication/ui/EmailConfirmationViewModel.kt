package it.lismove.app.android.authentication.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import it.lismove.app.android.R
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import kotlinx.coroutines.delay
import timber.log.Timber
import java.lang.Exception

class EmailConfirmationViewModel(
    val authRepository: AuthRepository,
    val userRepository: UserRepository): ViewModel() {
    var alreadyUpdated = false
    fun getUserState() = liveData<Boolean> {
        try {
            var mailConfirmed = false
            while (!mailConfirmed){
                val user = authRepository.getCurrentAuthUserReloaded()
                mailConfirmed = user?.isEmailVerified ?: false
                if(mailConfirmed && user != null && !alreadyUpdated){
                    val lismoveUser = userRepository.fetchUserProfile(user.uid).apply { emailVerified = true }
                    userRepository.updateUserProfile(lismoveUser)
                    alreadyUpdated = true
                }
                emit(mailConfirmed)
                delay(2000)
            }
        }catch (e: Exception){
            Timber.d(e.localizedMessage)
        }

    }

    fun getUserMailOrError(ctx: Context): String{
            try {
                val mail = authRepository.getCurrentAuthUser()?.email
                if(mail != null){
                    return ctx.getString(R.string.emailConfirmationDescription, mail)
                }else{
                    return "Si è verificato un errore"
                }
            }catch (e: Exception){
                return e.localizedMessage ?: "FSi è verificato un errore"
        }
    }

    fun sendEmailAgain(){
        authRepository.getCurrentAuthUser()?.sendEmailVerification()
    }

    fun signOut(){
        authRepository.signOut()
    }
}

