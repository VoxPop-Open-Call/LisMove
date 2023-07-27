package it.lismove.app.android.general.network

import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


object OkHttpClientProvider{
    fun getOkHttpClient(authInterceptor: AuthInterceptor,appDebugInterceptor: AppDebugInterceptor, authenticator: Authenticator): OkHttpClient {
        val networkErrorInterceptor = NetworkErrorInterceptor()
        val debugInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient().newBuilder()
            .readTimeout(1, TimeUnit.MINUTES)
            .authenticator(authenticator)
            .addInterceptor(appDebugInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(debugInterceptor)
            .addInterceptor(networkErrorInterceptor)
            .build()
    }
}