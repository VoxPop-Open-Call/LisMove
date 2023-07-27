package it.lismove.app.android.prefs

interface AlertPreferencesRepository {
    fun showManualPauseAlert(): Boolean
    fun setShowManualPauseAlert(show: Boolean)
}