package it.lismove.app.android.authentication.useCases.impl

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.ui.data.EmailSignInState
import it.lismove.app.android.authentication.useCases.EmailSignInUseCase
import timber.log.Timber
import java.lang.Exception

class EmailSignInUseCaseImpl(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
): EmailSignInUseCase {

    override suspend fun sign(email: String, password: String): EmailSignInState {
        try{
            val loginSuccess = authRepository.signIn(email, password)
            return if(loginSuccess){
                EmailSignInState.LoginSuccess(loggedIn = loginSuccess)
            }else{
                EmailSignInState.GenericError("Credenziali non corrette")
            }
        }catch (e: FirebaseAuthInvalidCredentialsException){
            Timber.d("Invalid user")
            val needMigration = userRepository.userNeedsResetPassword(email)
            return if(needMigration){
                EmailSignInState.ChangePasswordRequired()
            }else{
                EmailSignInState.GenericError("Credenziali non corrette")
            }
        }
    }

    override fun resetPassword(email: String) {
        authRepository.sendResetPassword(email)
    }
}