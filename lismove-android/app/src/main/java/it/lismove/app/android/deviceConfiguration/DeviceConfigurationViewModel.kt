package it.lismove.app.android.deviceConfiguration

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import it.lismove.app.android.chat.ChatManager
import it.lismove.app.android.chat.WhatsAppUtils
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SensorEntity
import it.lismove.app.common.DateTimeUtils
import kotlinx.coroutines.*
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.models.LisMoveDevice
import net.nextome.lismove_sdk.sensorUpgrade.DeviceUpgradeActivity
import net.nextome.lismove_sdk.sensorUpgrade.SensorUpgradeRepo
import net.nextome.lismove_sdk.statusListener.BluetoothStatusListener
import net.nextome.lismove_sdk.utils.BluetoothMedic
import net.nextome.lismove_sdk.utils.BugsnagUtils

class DeviceConfigurationViewModel(
    private val repo: SensorRepository,
    private val sensorSdk: LismoveSensorSdk,
    private val sensorRepository: SensorRepository,
    private val chatManager: ChatManager,
    private val sensorUpgradeRepo: SensorUpgradeRepo,
    private val user: LisMoveUser,
): ViewModel() {

    var wheelDiameterInMm: Int = SensorEntity.SENSOR_ENTINTY_DEFAUTL_WHEEL_DIAMETER
    var bikeType: String = SensorEntity.SENSOR_ENTITY_NORMAL
    val eventsObservable = LiveEvent<EVENTS>(config = LiveEventConfig.PreferFirstObserver)
    val sensorUpdateObservable = MutableLiveData<String>()

    var chatId: String? = null

    enum class EVENTS{
        SHOW_OPEN_CHAT_DIALOG
    }

    fun setWheelDimen(data: String){
        val wheelSize = WheelUtils.getWheelInMM(data)
        wheelDiameterInMm = wheelSize
    }

    fun setBikeTypeValue(data: String){
        bikeType = data
    }

    fun scanForBleSensor(ctx: Context) = liveData<Lce<LisMoveDevice>> {
        emit(LceLoading())

        try {
            val device = sensorSdk.scanAndReturnDevice()
            if (device != null) {
                saveSensorAndConnect(device, ctx)
                emit(LceSuccess(device))
            } else {
                BluetoothMedic.getInstance().resetBluetooth()
                BugsnagUtils.reportIssue(Exception("No sensor found during association."), BugsnagUtils.ErrorSeverity.INFO)
                emit(LceError(Exception("Sensore non trovato.")))
            }
        } catch (e: Exception) {
            BluetoothMedic.getInstance().resetBluetooth()

            BugsnagUtils.reportIssue(e)
            emit(LceError(Exception("Si è verificato un problema inaspettato nell'utilizzo del Bluetooth. Se l'errore persiste, per favore, riavvia il telefono.")))
        }
    }

    private suspend fun saveSensorAndConnect(device: LisMoveDevice, ctx: Context) {
        //val bikeType = if(bikeType == "Normale") 0 else 1
        val sensor = SensorEntity(
            user.uid,
            device.macAddress,
            device.name,
            wheelDiameterInMm,
            bikeType = bikeType,
            startAssociation = DateTimeUtils.getCurrentTimestamp(),
        )

        repo.addSensor(user.uid, sensor)

        // Establish connection to sensor
        sensorSdk.scanAndConnectToSensorIfNecessary(sensor.uuid, wheelDiameterInMm.toFloat(), sensor.hubCoefficient)
        upgradeSensorIfNecessary(sensor.uuid)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setLocalPairingDoneAsync() {
        // This is run in global scope, since the user can
        // quickly close the pairing activity after sensor association.
        // We want to make sure that the write doesn't get cancelled.
        GlobalScope.launch {
            sensorRepository.setSensorFirstPairingDone()
        }
    }

    fun disconnectAll() {
        sensorSdk.disconnectFromSensor()
    }

    fun contactAssistance(activity: AppCompatActivity){
        if(chatManager.isEnabled()){
            onChatRequested(activity)
        }else{
            WhatsAppUtils.openDefaultChat(activity)
        }
    }

    private fun onChatRequested(activity: AppCompatActivity){
        viewModelScope.launch {
            chatId = getChatIdOrNull()
            if(chatId.isNullOrEmpty()){
                openNewChat()
            }else{
                eventsObservable.value = EVENTS.SHOW_OPEN_CHAT_DIALOG
            }
        }
    }
    fun openExistentChat(){
        chatId?.let {
            chatManager.openChat(it)
        }
    }
    fun closeExistentAndCreateNewChat(){
        chatId?.let {
            chatManager.closeChat(it)
            chatId = null
            openNewChat()
        }
    }

    fun upgradeSensorIfNecessary(sensorMacAddress: String) {
        if (!sensorSdk.hasLatestFirmware()) {
            sendSensorUpdateEvent(sensorMacAddress)
        }
    }

    private fun sendSensorUpdateEvent(sensorMacAddress: String) {
        sensorUpdateObservable.value = sensorMacAddress
    }

    fun startSensorUpgrade(sensorMacAddress: String, ctx: Activity) {
        ctx.startActivity(DeviceUpgradeActivity.getIntent(sensorMacAddress, ctx))
        ctx.finish()
    }

    private fun openNewChat() {
        //TODO: Define message
        val message = "Non riesco a configurare il sensore"
        chatManager.openNewChat(message)
    }

    fun isBluetoothEnabled() = BluetoothStatusListener.isBluetoothEnabled()

    private suspend fun getChatIdOrNull():  String?{
        return withContext(Dispatchers.IO){
            chatManager.getOpenedChatOrNull()
        }
    }

    suspend fun willForceSensorUpdate() = sensorUpgradeRepo.willForceSensorUpdate()
}