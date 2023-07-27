package it.lismove.app.android.general.network

import android.content.Context
import android.net.ConnectivityManager


fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}