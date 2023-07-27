package net.nextome.lismove_sdk.utils

object LisMoveSensorUtils {
    fun isInDfuMode(deviceName: String?): Boolean {
        if (deviceName == null) return false
        return deviceName.contains("_OTA") || deviceName.contains("BK463U") || deviceName.contains("BK463")
    }
}