package it.lismove.app.android.authentication.repository

interface PhoneRepository {
    suspend fun getInstallationIdentifier(): String
    fun getDeviceName(): String
    fun getAppVersion(): String
}