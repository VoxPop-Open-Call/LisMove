package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.ui.data.SignUpState
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber


class SignInViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    private val stateFlow = MutableStateFlow<Lce<Boolean>>(LceSuccess(false))
    val state = stateFlow.asLiveData()


    fun signUpWithGoogleIntent(data: Intent?){
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            signUpWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            viewModelScope.launch {stateFlow.emit(LceError(e))}
        }
    }

    private fun signUpWithGoogle(idToken: String){
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

    private fun performGoogleSignUp(idToken: String) = flow<Lce<Boolean>>{
        val res = authRepository.firebaseAuthWithGoogle(idToken)
        stateFlow.emit(LceSuccess(res != null))
    }.catch { emit(LceError(it)) }

    private fun performFacebookSignUp(token: AccessToken) = flow<Lce<Boolean>> {
        val res = authRepository.firebaseAuthWithFacebook(token)
        emit(LceSuccess(res != null))
    }.catch { emit(LceError(it)) }



}