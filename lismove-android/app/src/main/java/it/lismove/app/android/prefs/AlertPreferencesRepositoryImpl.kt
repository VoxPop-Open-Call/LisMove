package it.lismove.app.android.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import it.lismove.app.android.general.LisMoveAppSettings
import timber.log.Timber

class AlertPreferencesRepositoryImpl(context: Context): AlertPreferencesRepository {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = LisMoveAppSettings.SHARED_PREFERENCES_KEY
    private val MANUAL_PAUSE_KEY = "PREF_SHOW_MANUAL_PAUSE"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    override fun showManualPauseAlert(): Boolean {
        val res =  sharedPref.getBoolean(MANUAL_PAUSE_KEY, true)
        Timber.d("showManualPauseAlert $res")
        return res
    }

    override fun setShowManualPauseAlert(show: Boolean) {
        sharedPref.edit {
            putBoolean(MANUAL_PAUSE_KEY, show)
        }
    }
}