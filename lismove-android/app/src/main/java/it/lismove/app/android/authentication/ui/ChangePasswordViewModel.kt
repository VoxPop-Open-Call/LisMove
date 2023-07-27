package it.lismove.app.android.authentication.ui

import androidx.lifecycle.ViewModel
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository

class ChangePasswordViewModel(
    val authRepository: AuthRepository,
    val userRepository: UserRepository
) : ViewModel(){

    fun registerUserAndChangePassword(email: String){

    }

}