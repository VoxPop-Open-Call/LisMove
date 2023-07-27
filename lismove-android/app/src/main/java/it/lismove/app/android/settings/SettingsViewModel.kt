package it.lismove.app.android.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.common.PreferencesKeys
import it.lismove.app.android.background_detect.SensorDetectionManager
import it.lismove.app.android.theme.ThemeRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.database.SessionSdkRepository
import timber.log.Timber

class SettingsViewModel(
        private val themeRepository: ThemeRepository,
        private val sessionSdkRepository: SessionSdkRepository,
        private val dataStore: DataStore<Preferences>,
    ): ViewModel() {
    var theme: Int = themeRepository.getTheme()

    fun setBackgroundSensorDetectionEnabled(value: Boolean, ctx: Context) = viewModelScope.launch {
        dataStore.edit { it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_ENABLED] = value }

        if (value) {
            if (sessionSdkRepository.getActiveSession() == null) {
                SensorDetectionManager.startNearbyDetection(ctx)
            }
        } else {
            SensorDetectionManager.stopNearbyDetection(ctx)
        }
    }

    fun setBackgroundSensorDetectionAutoStartEnabled(value: Boolean, ctx: Context) = viewModelScope.launch {
        dataStore.edit { it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_AUTO_START_ENABLED] = value }
    }

    fun backgroundSensorDetectionEnabledObservable() = dataStore.data.map { it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_ENABLED] ?: false }.asLiveData()
    fun backgroundSensorDetectionAutoStartEnabledObservable() = dataStore.data.map {it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_AUTO_START_ENABLED] ?: false}.asLiveData()

    fun changeTheme(){
        if(theme == AppCompatDelegate.MODE_NIGHT_YES) {
            Timber.d("Theme no night")
            theme = AppCompatDelegate.MODE_NIGHT_NO
            themeRepository.setTheme( AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            Timber.d("Theme no night")
            theme = AppCompatDelegate.MODE_NIGHT_YES
            themeRepository.setTheme( AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}