package it.lismove.app.android.authentication.useCases

import it.lismove.app.android.authentication.useCases.data.LoginState

interface AuthenticationUseCase {
    suspend fun fetchUserAuthenticationState(): LoginState
}