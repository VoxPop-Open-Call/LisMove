package it.lismove.app.android.authentication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.ui.data.SignUpState
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.utils.isEmailValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.Error

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val stateFlow = MutableStateFlow<Lce<SignUpState>>(LceSuccess(SignUpState()))
    val state = stateFlow.asLiveData()

    fun checkInputAndSignIn(email: String, password: String, confirmPassword: String){
        viewModelScope.launch {
            if(isEmailValid(email) && isPasswordValid(password) && arePasswordTheSame(password, confirmPassword)){
                stateFlow.emitAll(performSignUp(email, password))
            }else{
                stateFlow.emit(LceSuccess(SignUpState(inputError = true)))
            }
        }
    }

    fun signUpWithGoogle(idToken: String){
        viewModelScope.launch {
            stateFlow.emitAll(performGoogleSignUp(idToken))
        }
    }

    fun signUpWithFacebook(token: AccessToken?){
        token?.let {
            viewModelScope.launch {
                stateFlow.emitAll(performFacebookSignUp(token))
            }
        }
    }

    fun isEmailValid(email: String): Boolean{
        return email.isEmailValid()
    }
    fun isPasswordValid(password: String): Boolean{
        return password.count() > 5
    }
    fun arePasswordTheSame(password: String, confirmPassword: String): Boolean{
        return password == confirmPassword
    }



    private fun performGoogleSignUp(idToken: String) = flow<Lce<SignUpState>>{
        val userId = authRepository.firebaseAuthWithGoogle(idToken)
        stateFlow.emit(LceSuccess(SignUpState(signedUp = userId != null)))

    }.catch {
        emit(LceError(it))
    }

    private fun performSignUp(email: String, password: String) = flow<Lce<SignUpState>> {
        emit(LceLoading())
        val res = authRepository.signUp(email, password)
        if(res != null){
            userRepository.createUserProfile(res, email)
        }
        authRepository.getCurrentAuthUser()?.sendEmailVerification()
        emit(LceSuccess(SignUpState(signedUp = res != null)))
    }.catch { emit(LceError(it)) }

    private fun performFacebookSignUp(token: AccessToken) = flow<Lce<SignUpState>> {
        val res = authRepository.firebaseAuthWithFacebook(token)
        emit(LceSuccess(SignUpState(signedUp =  res != null)))
    }.catch { emit(LceError(it)) }


}
