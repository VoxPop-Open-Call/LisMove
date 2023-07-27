package it.lismove.app.utils

import android.content.Context
import android.content.SharedPreferences
import it.lismove.app.android.general.utils.fromJson
import it.lismove.app.android.general.utils.toJson
import it.lismove.app.room.entity.LisMoveUser

class TempPrefsRepositoryImpl(context: Context): TempPrefsRepository {
    private val PREF_NAME = "lismove_temp_prefs"
    private val PREF_KEY_USER = "temp_user"
    private val PREF_CONFIG_SENT = "config_sent"
    private val PREF_OPTIMIZATION_SENT = "optimization_sent"
    private val PREF_FLOATING_OPEN = "floating_open"

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun saveTempUser(user: LisMoveUser) {
        with (sharedPrefs.edit()) {
            putString(PREF_KEY_USER, user.toJson())
            apply()
        }
    }

    override fun getTempUser(): LisMoveUser {
        return sharedPrefs.getString(PREF_KEY_USER, "")!!.fromJson()
    }

    override fun setConfigSent(value: Boolean) {
        with (sharedPrefs.edit()) {
            putBoolean(PREF_CONFIG_SENT, value)
            apply()
        }
    }

    override fun isConfigSent(): Boolean {
        return sharedPrefs.getBoolean(PREF_CONFIG_SENT, false)
    }

    override fun setOptimizationSent(value: Boolean) {
        with (sharedPrefs.edit()) {
            putBoolean(PREF_OPTIMIZATION_SENT, value)
            apply()
        }
    }

    override fun isOptimizationSent(): Boolean {
        return sharedPrefs.getBoolean(PREF_OPTIMIZATION_SENT, false)
    }

    override fun setSessionFloatingOpen(value: Boolean) {
        with (sharedPrefs.edit()) {
            putBoolean(PREF_FLOATING_OPEN, value)
            apply()
        }
    }

    override fun isSessionFloatingOpen(): Boolean {
        return sharedPrefs.getBoolean(PREF_FLOATING_OPEN, false)
    }
}