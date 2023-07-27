package it.lismove.app.android.general.network

import okhttp3.Request

object RequestHelper{
    /**
     * If the request don't need auth just put an header in the API interface
     * @Headers("No-Authentication: true")
     */
    fun isRequestWithAuth(request: Request): Boolean{
        return request.header(NO_AUTH_HEADER) == null
    }
    const val RETRY_HEADER = "RetryHeader"
    const val TOKEN_HEADER = "Authorization"
    const val OS_TYPE = "app-os"
    const val OS_VERSION = "app-version"
    const val NO_AUTH_HEADER = "No-Authentication"
}