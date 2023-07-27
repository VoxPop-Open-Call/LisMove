package it.lismove.app.android.authentication.ui

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.ui.data.EmailSignInState
import it.lismove.app.android.authentication.useCases.EmailSignInUseCase
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.utils.isEmailValid
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class EmailSignInViewModel(
    private val emailSignInUseCase: EmailSignInUseCase
): ViewModel() {

    private var stateFlow =  MutableStateFlow<EmailSignInState>((EmailSignInState.InitialState()))
    var state = stateFlow.asLiveData()
    var emailInserted: String? = null
    private var passwordMinChar = 6

    fun checkInputAndSignIn(email: String?, password: String?){
        viewModelScope.launch {
            if(checkInputParameters(email, password)){
               stateFlow.emitAll(signIn(email!!, password!!))
            }else{
               sendInputParametersError(email, password)
            }
        }

    }

    private fun signIn(email: String, password: String) = flow {
        emailInserted = email
        emit(EmailSignInState.Loading())
        emit(emailSignInUseCase.sign(email, password))
    }.catch { emit(EmailSignInState.GenericError(it.localizedMessage)) }

    private fun checkInputParameters(email: String?, password: String?): Boolean{
        val emailValid = isEmailValid(email)
        val passwordValid = isPasswordValid(password)
        return emailValid && passwordValid
    }

    private fun sendInputParametersError(email: String?, password: String?){
        val emailMessage = if (isEmailValid(email)) null else "Inserire una mail Valida"
        val passwordMessage = if (isPasswordValid(password)) null else "Inserire una passwrod di almeno $passwordMinChar caratteri"
        viewModelScope.launch { stateFlow.emit(EmailSignInState.InputFieldError(passwordMessage, emailMessage))}
    }

    private fun isEmailValid(email: String?): Boolean{
        return email?.isEmailValid() ?: false
    }
    private fun isPasswordValid(password: String?): Boolean{
        var flag = false
        password?.let { flag =  password.count() > passwordMinChar - 1 }
        return  !TextUtils.isEmpty(password) && flag
    }


    fun resetPassword(){
        emailInserted?.let {
            emailSignInUseCase.resetPassword(it)

        }
    }

}