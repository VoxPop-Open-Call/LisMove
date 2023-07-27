package it.lismove.app.android.deviceConfiguration.repository

import android.content.Context
import android.content.SharedPreferences
import it.lismove.app.android.R
import it.lismove.app.android.deviceConfiguration.SessionConfig
import it.lismove.app.android.general.utils.fromJson
import it.lismove.app.android.general.utils.toJson

class SessionConfigRepositoryImpl(
    val context: Context
): SessionConfigRepository {

    private val configKey = "device_config"

    override fun saveSessionConfig(config: SessionConfig) {
        with (getPrefs().edit()) {
            putString(configKey, config.toJson())
            apply()
        }
    }

    override fun loadSessionConfig(): SessionConfig? {
        with (getPrefs()) {
            if (contains(configKey).not()) { return null }
            return getString(configKey, "")?.fromJson()
        }
    }

    override fun clearSessionConfig() {
        with (getPrefs().edit()) {
            remove(configKey)
            apply()
        }
    }

    private fun getPrefs(): SharedPreferences {
        with (context) {
            return getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        }
    }
}