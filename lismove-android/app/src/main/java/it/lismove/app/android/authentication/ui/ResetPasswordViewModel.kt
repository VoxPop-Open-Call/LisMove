package it.lismove.app.android.authentication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.utils.isEmailValid
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ResetPasswordViewModel(
    val authRepository: AuthRepository,
    val userRepository: UserRepository
) : ViewModel(){
    fun isEmailValid(email: String): Boolean{
        return email.isEmailValid()
    }

    fun sendResetPasswordRequest(email: String): LiveData<ResetPasswordState> = flow {
        emit(Loading)
        if(userRepository.existsUser(email)){
            authRepository.sendResetPassword(email)
            emit(ResetRequested)
        }else{
            emit(EmailNotFound)
        }

    }.catch { emit(Error(it.localizedMessage ?: "Si è verificato un errore!")) }.asLiveData()

}

sealed class ResetPasswordState

object ResetRequested: ResetPasswordState()
object EmailNotFound: ResetPasswordState()
object Loading: ResetPasswordState()
class Error(val message: String): ResetPasswordState()