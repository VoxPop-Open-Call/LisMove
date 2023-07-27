package it.lismove.app.android.general.network

import it.lismove.app.android.authentication.repository.AuthRepository
import okhttp3.*
import timber.log.Timber

class AuthInterceptor(private val authRepository: AuthRepository): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()
        if(RequestHelper.isRequestWithAuth(req)) {
            val authToken = authRepository.getUserToken()
            Timber.d("token in header is $authToken")
            req = req.newBuilder().addHeader(RequestHelper.TOKEN_HEADER, "Bearer $authToken").build()

        }
        return chain.proceed(req)
    }
}






