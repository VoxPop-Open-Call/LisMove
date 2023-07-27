package it.lismove.app.android.general.network

import it.lismove.app.android.authentication.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class LisMoveAuthenticator(private val authRepository: AuthRepository): Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        var retryCount = response.request.header(RequestHelper.RETRY_HEADER)?.toInt() ?: 0
        if (RequestHelper.isRequestWithAuth(response.request) && retryCount < 2) {
            //refresh token only if the request need auth and we didn't
            // reach the repeat limit required to prevent loop
            return runBlocking {
                authRepository.refreshUserToken()?.let {
                    Timber.d("token refreshed is RETRY_HEADER $it")
                    val res = response.request.newBuilder()
                            .header(RequestHelper.TOKEN_HEADER, "Bearer $it")
                            .header(RequestHelper.RETRY_HEADER, "${++retryCount}")
                            .build()
                    Timber.d("request refreshed with header: ${res.headers}")
                    return@runBlocking res
                }
                Timber.d("token refreshed but null")
                return@runBlocking null
            }
        } else {
            Timber.d("token no refreshed $retryCount")
            return null
        }
    }
}