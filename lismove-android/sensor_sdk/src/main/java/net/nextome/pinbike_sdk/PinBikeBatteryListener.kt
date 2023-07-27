package net.nextome.lismove_sdk

import net.nextome.lismove_sdk.models.BatteryPrimitive

interface LisMoveBatteryListener {
    fun onBatteryAvailable(battery: BatteryPrimitive)
}