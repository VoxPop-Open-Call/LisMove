package it.lismove.app.android.prefs

interface LatestVersionRepository {
    suspend fun getLatestVersion(): Int
    suspend fun getLatestVersionRequired(): Int
}