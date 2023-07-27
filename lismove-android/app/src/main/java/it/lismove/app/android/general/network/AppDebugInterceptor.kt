package it.lismove.app.android.general.network

import android.content.Context
import android.content.pm.PackageInfo
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AppDebugInterceptor(val context: Context): Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader(RequestHelper.OS_TYPE, "android")
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version: String = pInfo.versionName
        Timber.d("version is $version")
        requestBuilder.addHeader(RequestHelper.OS_VERSION, version)
        return chain.proceed(requestBuilder.build())
    }
}