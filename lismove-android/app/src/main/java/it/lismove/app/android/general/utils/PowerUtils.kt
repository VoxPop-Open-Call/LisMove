package it.lismove.app.android.general.utils

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import it.lismove.app.android.general.LisMoveAppSettings
import it.lismove.app.utils.TempPrefsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber


object PowerUtils: KoinComponent {

    private val tempPrefsRepository: TempPrefsRepository by inject()

    fun checkIfBatteryOptimized(ctx: Context) {
        if (willCheck(ctx)) {
            val powerManager =
                ctx.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

            if (!powerManager.isIgnoringBatteryOptimizations(ctx.applicationContext.packageName)) {
                showIgnoreBatteryOptimizationDialog(ctx)
                Timber.d("Send")

                tempPrefsRepository.setOptimizationSent(true)
            } else {
                // do nothing, already in whitelist
            }
        }
    }

    const val PREFS_PARAM_NAME = "ask_optimize"

    private fun willCheck(ctx: Context): Boolean {
        val PREF_NAME = LisMoveAppSettings.SHARED_PREFERENCES_KEY
        val PRIVATE_MODE = 0

        val sharedPref: SharedPreferences = ctx.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

        val alreadyAsked = tempPrefsRepository.isOptimizationSent()
        val shouldAsk = sharedPref.getBoolean(PREFS_PARAM_NAME, true)

        return shouldAsk && !alreadyAsked
    }

    private fun setDoNotAskAgain(ctx: Context) {
        val PREF_NAME = LisMoveAppSettings.SHARED_PREFERENCES_KEY
        val PRIVATE_MODE = 0

        val sharedPref: SharedPreferences = ctx.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        sharedPref.edit().putBoolean(PREFS_PARAM_NAME, false).apply()
    }

    private fun showIgnoreBatteryOptimizationDialog(ctx: Context) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(ctx)

        alertDialogBuilder.setTitle("Ottimizzazioni batteria attive")
        alertDialogBuilder.setIcon(R.drawable.ic_lock_idle_low_battery)

        // set dialog message
        alertDialogBuilder
            .setMessage("Lis Move non è tra le app esenti dalle ottimizzazioni della batteria.\n\nPer un'esperienza utente migliore, seleziona \"Non ottimizzare\" Lis Move nelle impostazioni di sistema.")
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog, _ ->
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                dialog.cancel()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

            .setNeutralButton("Non chiedere più") { dialog, _ ->
                setDoNotAskAgain(ctx)
                dialog.cancel()
            }

        alertDialogBuilder.create().show()
    }
}