package it.lismove.app.android.authentication.useCases.impl

import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.PhoneRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.useCases.AuthenticationUseCase
import it.lismove.app.android.authentication.useCases.data.*
import it.lismove.app.android.chat.ChatManager
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import java.lang.Exception

class AuthenticationUseCaseMockImpl(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val phoneRepository: PhoneRepository,
    private val chatManager: ChatManager
): AuthenticationUseCase, KoinComponent {

    override suspend fun fetchUserAuthenticationState(): LoginState {
        delay(3000)
        throw Exception()
    }

}