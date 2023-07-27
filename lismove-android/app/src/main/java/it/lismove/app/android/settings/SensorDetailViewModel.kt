package it.lismove.app.android.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SensorEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import net.nextome.lismove_sdk.database.SessionSdkRepository

class SensorDetailViewModel(
        private val sensorRepository: SensorRepository,
        private val sessionSdkRepository: SessionSdkRepository,
        private val user: LisMoveUser
        ): ViewModel() {
    var sensor: SensorEntity? = null
    val sensorStolenDuringSessionError = "Hai una sessione in corso, termina la sessione prima di poter segnalare il sensore come rubato"
    val sensorDisassociateDuringSessionError =
        "Hai una sessione in corso, termina la sessione prima di poter disassociare il sensore"

    fun getSensorData(): LiveData<Lce<SensorEntity?>> = flow{
        emit(LceLoading())
        sensor = sensorRepository.getSensor(user.uid)
        emit(LceSuccess(sensor))
    }.catch { emit(LceError(it)) }.asLiveData()

    fun setSensorStolen(sensorId: String): LiveData<Lce<SensorEntity>> = flow{
        if(isSessionInProgress()){
            throw Exception(sensorStolenDuringSessionError)
        }
        emit(LceLoading())
        val sensor = sensorRepository.setStolen(user.uid, sensorId)
        emit(LceSuccess(sensor))
    }.catch { emit(LceError(it)) }.asLiveData()

    suspend fun isSessionInProgress(): Boolean {
        return sessionSdkRepository.getActiveSession() != null
    }

    suspend fun disassociateSensor(){
        if(isSessionInProgress()){

            throw Exception(sensorDisassociateDuringSessionError)
        }else{
            sensor?.let {
                sensorRepository.removeSensor(user.uid,it.uuid)
            }
        }

    }

}
