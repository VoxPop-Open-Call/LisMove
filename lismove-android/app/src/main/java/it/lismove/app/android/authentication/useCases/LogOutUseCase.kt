package it.lismove.app.android.authentication.useCases

interface LogOutUseCase {
    suspend fun logOut(forceStopSession: Boolean)
}