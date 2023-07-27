package it.lismove.app.android.authentication.useCases

import it.lismove.app.android.authentication.ui.data.EmailSignInState

interface EmailSignInUseCase {
    suspend fun sign(email:String, password: String): EmailSignInState
    fun resetPassword(email: String)
}