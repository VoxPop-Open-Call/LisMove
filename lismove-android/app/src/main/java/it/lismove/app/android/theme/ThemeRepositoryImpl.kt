package it.lismove.app.android.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import it.lismove.app.android.general.LisMoveAppSettings
import it.lismove.app.android.general.utils.getDefaultTheme
import it.lismove.app.android.general.utils.isDarkThemeOn

class ThemeRepositoryImpl(val context: Context): ThemeRepository {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = LisMoveAppSettings.SHARED_PREFERENCES_KEY
    private val THEME_NAME = "PREF_THEME"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    override fun getTheme(): Int {
       return sharedPref.getInt(THEME_NAME, context.getDefaultTheme())
    }

    override fun setTheme(theme: Int) {
        sharedPref.edit {
            putInt(THEME_NAME, theme)
        }
    }

    override fun resetTheme() {
        sharedPref.edit {
            putInt(THEME_NAME, AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}

