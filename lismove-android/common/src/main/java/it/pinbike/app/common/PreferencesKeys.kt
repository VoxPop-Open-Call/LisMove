package it.lismove.app.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object PreferencesKeys {
    val PREF_IS_APP_FOREGROUND = booleanPreferencesKey("PREF_IS_APP_FOREGROUND")
    val PREF_IS_BACKGROUND_SENSOR_DETECTION_ENABLED = booleanPreferencesKey("PREF_IS_BACKGROUND_SENSOR_SCAN_ENABLED")
    val PREF_IS_BACKGROUND_SENSOR_DETECTION_AUTO_START_ENABLED = booleanPreferencesKey("PREF_IS_BACKGROUND_SENSOR_DETECTION_AUTO_START_ENABLED")
    val PREF_LAST_SENSOR_DETECTION_NOTIFICATION_TS = longPreferencesKey("PREF_LAST_SENSOR_DETECTION_NOTIFICATION_TS")
    val PREF_FIRST_SENSOR_PAIRING_DONE = booleanPreferencesKey("PREF_FIRST_SENSOR_PAIRING_DONE")
    val PREF_FORCE_SENSOR_UPDATE = booleanPreferencesKey("PREF_FORCE_SENSOR_UPDATE")
}