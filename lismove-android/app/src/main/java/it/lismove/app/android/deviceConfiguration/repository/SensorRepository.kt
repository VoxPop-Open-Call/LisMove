package it.lismove.app.android.deviceConfiguration.repository

import it.lismove.app.room.entity.SensorEntity

interface SensorRepository {
    suspend fun addSensor(userId: String, sensor: SensorEntity)
    suspend fun getSensor(userId: String): SensorEntity?
    suspend fun removeSensor(userId: String, sensorId: String)
    suspend fun getLocalCachedSensor(userId: String): SensorEntity?
    suspend fun setStolen(userId: String, sensorId: String): SensorEntity
    suspend fun setSensorFirstPairingDone()
    suspend fun isSensorFirstPairingDone(): Boolean
    suspend fun updateFirmwareToServerIfNecessary(userId: String, newFirmware: String)
}