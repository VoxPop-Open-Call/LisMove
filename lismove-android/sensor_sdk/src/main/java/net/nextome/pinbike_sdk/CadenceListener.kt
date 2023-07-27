package net.nextome.lismove_sdk

import net.nextome.lismove_sdk.models.CadencePrimitive
import net.nextome.lismove_sdk.models.LisMoveSensorHistoryElement

interface CadenceListener {
    fun onCadenceAvailable(cadence: CadencePrimitive)
    fun onBatteryAvailable(battery: Int?)
    // only on v2.5
    fun onHistoryAvailable(historyData: List<LisMoveSensorHistoryElement>)
}