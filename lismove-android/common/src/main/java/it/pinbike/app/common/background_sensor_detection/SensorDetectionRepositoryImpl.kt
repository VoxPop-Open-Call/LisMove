package it.lismove.app.common.background_sensor_detection

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import it.lismove.app.common.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SensorDetectionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
): SensorDetectionRepository {
    override suspend fun getLastSensorDetectionTs(): Long = dataStore.data.map { it[PreferencesKeys.PREF_LAST_SENSOR_DETECTION_NOTIFICATION_TS] }.first() ?: 0L
    override suspend fun setLastSensorDetectionTs(ts: Long) { dataStore.edit { it[PreferencesKeys.PREF_LAST_SENSOR_DETECTION_NOTIFICATION_TS] = ts } }
}