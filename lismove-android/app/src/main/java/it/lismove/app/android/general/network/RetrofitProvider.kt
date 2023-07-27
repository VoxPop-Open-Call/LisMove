package it.lismove.app.android.general.network


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.ResponseBody
import retrofit2.Converter
import java.lang.reflect.Type


object RetrofitProvider {
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}

