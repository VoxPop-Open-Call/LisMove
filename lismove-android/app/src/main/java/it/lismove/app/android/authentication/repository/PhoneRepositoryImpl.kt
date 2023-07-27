package it.lismove.app.android.authentication.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.tasks.await

class PhoneRepositoryImpl(
    val context: Context
): PhoneRepository {
    override suspend fun getInstallationIdentifier(): String {
       return FirebaseInstallations.getInstance().id.await()
    }

    override fun getDeviceName(): String {
        return getDeviceNameAndModel()
    }
    private fun getDeviceNameAndModel(): String {
        val manufacturer: String = Build.MANUFACTURER
        val model: String = Build.MODEL
        val androidVersion: String = Build.VERSION.RELEASE
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            capitalize(model) + " | " + androidVersion
        } else {
            capitalize(manufacturer) + " " + model + " | " + androidVersion
        }
    }


    private fun capitalize(s: String?): String {
        if (s.isNullOrEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    override fun getAppVersion(): String {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version: String = pInfo.versionName
        return version
    }
}