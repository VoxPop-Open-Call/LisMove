package it.lismove.app.android.prefs

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.lismove.app.android.general.network.NetworkConfig
import kotlinx.coroutines.tasks.await

class LatestVersionRepositoryImpl: LatestVersionRepository {

    override suspend fun getLatestVersion(): Int {
        val database = Firebase.database(NetworkConfig.FIREBASE_RTDB_URL)
        return try {
            database.reference
                .child("latestVersion")
                .child("android")
                .get()
                .await()
                .getValue(Int::class.java) ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun getLatestVersionRequired(): Int {
        val database = Firebase.database(NetworkConfig.FIREBASE_RTDB_URL)
        return try {
            database.reference
                .child("latestVersionRequired")
                .child("android")
                .get()
                .await()
                .getValue(Int::class.java) ?: -1
        } catch (e: Exception) {
            -1
        }
    }
}