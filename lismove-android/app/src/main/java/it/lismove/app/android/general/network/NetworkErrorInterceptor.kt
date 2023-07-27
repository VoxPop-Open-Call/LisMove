package it.lismove.app.android.general.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.net.UnknownHostException

class NetworkErrorInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request()
            val response = chain.proceed(request)

            if (!response.isSuccessful){
                Timber.e("HTTP ERROR: ${response.code}")
                // Timber.e("Body ${response.body?.string()}")

                throw LismoveNetworkException(response.body?.string())
            }

            return response
        } catch (e: UnknownHostException) {
            throw UnknownHostException("Nessuna connessione ad internet disponibile.")
        }
    }
}