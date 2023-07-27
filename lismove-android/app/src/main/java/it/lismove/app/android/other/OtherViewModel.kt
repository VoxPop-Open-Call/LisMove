package it.lismove.app.android.other

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel

class OtherViewModel(val context: Context): ViewModel() {

    fun getVersionNumber(): String{
        val pInfo: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
        val version: String = pInfo.versionName
        return "Lis Move v$version"
    }
}