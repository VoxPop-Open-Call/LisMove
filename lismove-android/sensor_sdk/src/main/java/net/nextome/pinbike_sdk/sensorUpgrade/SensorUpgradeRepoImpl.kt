package net.nextome.lismove_sdk.sensorUpgrade

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import it.lismove.app.common.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SensorUpgradeRepoImpl(
    private val dataStore: DataStore<Preferences>,
): SensorUpgradeRepo {
    override suspend fun willForceSensorUpdate(): Boolean {
        return false
        // return dataStore.data.map { it[PreferencesKeys.PREF_FORCE_SENSOR_UPDATE] }.first() ?: true
    }

    override suspend fun setForceSensorUpdate(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.PREF_FORCE_SENSOR_UPDATE] = value }
    }
}