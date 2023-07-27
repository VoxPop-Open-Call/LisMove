package net.nextome.lismove_sdk

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager.EXTRA_LEVEL
import android.util.Log

class BatteryManager {
     fun getBatteryLevel(context: Context): Int? {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
        val scale =  batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
         if(level != null && scale != null){
             Log.e("BatteryLevelLismove", "Phone: $level / $scale * 100")
             return level.div(scale.toFloat()).times(100).toInt()
         }else{
             return null
         }

    }


}