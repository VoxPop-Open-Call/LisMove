package net.nextome.lismove_sdk.sensorUpgrade

interface SensorUpgradeRepo {
    // TODO: Always false for prod release
    suspend fun willForceSensorUpdate(): Boolean
    suspend fun setForceSensorUpdate(value: Boolean)
}