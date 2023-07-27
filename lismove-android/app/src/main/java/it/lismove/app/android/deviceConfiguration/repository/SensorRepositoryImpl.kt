package it.lismove.app.android.deviceConfiguration.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import it.lismove.app.android.deviceConfiguration.apiService.SensorApi
import it.lismove.app.android.general.network.LismoveNetworkException
import it.lismove.app.common.PreferencesKeys
import it.lismove.app.room.dao.SensorDao
import it.lismove.app.room.entity.SensorEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

class SensorRepositoryImpl(
    private val sensorApi: SensorApi,
    private val sensorDao: SensorDao,
    private val dataStore: DataStore<Preferences>,
): SensorRepository {
    override suspend fun updateFirmwareToServerIfNecessary(userId: String, newFirmware: String) {
        val oldSensor = getSensor(userId)
        Log.e("firmwareUpdate", "Uploading firmware info")
        if (oldSensor != null) {
            if (newFirmware != oldSensor.firmware) {
                Log.e("firmwareUpdate", "New firmware: ${newFirmware} != ${oldSensor.firmware}")
                oldSensor.apply {
                    startAssociation = System.currentTimeMillis()
                    endAssociation = null
                    firmware = newFirmware
                }

                addSensor(userId, oldSensor)
            }
        }
    }

    override suspend fun addSensor(userId: String, sensor: SensorEntity) {
       val response =  sensorApi.addSensor(userId, sensor)
        saveSensorUpdate(userId, response)
    }

    override suspend fun getSensor(userId: String): SensorEntity? {
        return try {
            // val response =  sensorApi.getActiveSensor(userId)
            val response = sensorApi.getActiveSensorList(userId).firstOrNull()?.apply {
                this.userId = userId
            } ?: return null

            sensorDao.addOrUpdate(response)
            response
        }catch (error: LismoveNetworkException){
            if(error.status == 404){
                return null
            }else{
                throw error
            }
        } catch (error: IOException){
            sensorDao.getSensor(userId)
        }
    }

    override suspend fun removeSensor(userId: String, sensorId: String) {
        sensorApi.disassociateSensor(userId, sensorId)
        sensorDao.deleteSensor(userId)
    }

    override suspend fun getLocalCachedSensor(userId: String): SensorEntity? {
        return try {
            sensorDao.getSensor(userId)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override suspend fun setStolen(userId: String, sensorId: String): SensorEntity {
        val response = sensorApi.setStolen(userId, sensorId)
        return saveSensorUpdate(userId, response)
    }

    private suspend fun saveSensorUpdate(userId: String, response: SensorEntity): SensorEntity{
        response.apply {
            this.userId = userId
        }
        sensorDao.addOrUpdate(response)
        return response
    }

    override suspend fun setSensorFirstPairingDone() {
        dataStore.edit { it[PreferencesKeys.PREF_FIRST_SENSOR_PAIRING_DONE] = true }
    }

    override suspend fun isSensorFirstPairingDone(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.PREF_FIRST_SENSOR_PAIRING_DONE] }.first() ?: false
    }
}