package it.lismove.app.android.background_detect

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import it.lismove.app.common.PreferencesKeys
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object SensorDetectionManager: KoinComponent {
    private const val WORKER_ID_SENSOR_DETECTION = "background_sensor_detection"
    private val datastore: DataStore<Preferences> by inject()

    fun startNearbyDetection(ctx: Context){
        GlobalScope.launch {
            if (isBackgroundScanEnabled(ctx)) {
                val sensorDetectionWorkerRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<SensorDetectionWorker>()
                        .addTag(WORKER_ID_SENSOR_DETECTION)
                        .build()

                WorkManager
                    .getInstance(ctx)
                    .enqueueUniqueWork(
                        WORKER_ID_SENSOR_DETECTION,
                        ExistingWorkPolicy.REPLACE,
                        sensorDetectionWorkerRequest
                    )
            }
        }
    }

    fun stopNearbyDetection(ctx: Context){
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WORKER_ID_SENSOR_DETECTION)
    }

    private suspend fun isBackgroundScanEnabled(ctx: Context): Boolean = datastore.data.map { it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_ENABLED]}.first() ?: false
}