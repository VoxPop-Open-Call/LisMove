package net.nextome.lismove_sdk.utils

import android.content.Context

object SessionDelayManager {

    private val DELAY_NAME = "PARTIAL_DELAY"
    private val DEFAULT_VALUE = 4 //seconds

    private val DELAY_REVISION = "PARTIAL_DELAY_REV"
    // increment to force new partial values to all
    private val currentRevision = 3

    fun setDelay(ctx: Context, value: Int){
        val sharedPref = ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(DELAY_NAME, value)
            apply()
        }
    }

    fun getDelay(ctx: Context): Int{
        val sharedPref = ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)

        if(sharedPref.getInt(DELAY_REVISION, 0) != currentRevision) {
            with (sharedPref.edit()) {
                putInt(DELAY_REVISION, currentRevision)
                putInt(DELAY_NAME, DEFAULT_VALUE)
                apply()
            }

            return DEFAULT_VALUE
        }

        return sharedPref.getInt(DELAY_NAME, DEFAULT_VALUE)
    }
}