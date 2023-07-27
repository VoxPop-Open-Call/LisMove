package it.lismove.app.android.general.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate

fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun Context.getDefaultTheme(): Int{
    return  if(isDarkThemeOn()){
        AppCompatDelegate.MODE_NIGHT_YES
    }else{
        AppCompatDelegate.MODE_NIGHT_NO

    }
}