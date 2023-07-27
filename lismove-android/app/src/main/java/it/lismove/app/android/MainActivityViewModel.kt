package it.lismove.app.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import it.lismove.app.android.background_detect.SensorDetectionManager
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.notification.data.NotificationMessageDelivery
import it.lismove.app.android.prefs.LatestVersionRepository
import it.lismove.app.android.session.repository.SensorHistoryRepository
import it.lismove.app.android.session.useCases.SessionCachingUseCase
import it.lismove.app.android.theme.ThemeRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.models.LisMoveBleState
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SensorEntity
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.sensorUpgrade.DeviceUpgradeActivity
import net.nextome.lismove_sdk.sensorUpgrade.SensorUpgradeRepo
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.core.component.KoinComponent
import timber.log.Timber
import kotlin.math.PI

class MainActivityViewModel(
    private val themeRepository: ThemeRepository,
    private val sensorRepository: SensorRepository,
    private var alertChecked: Boolean,
    private val sensorSdk: LismoveSensorSdk,
    private val sensorHistoryRepository: SensorHistoryRepository,
    private val sessionCachingUseCase: SessionCachingUseCase,
    private val sessionSdkRepository: SessionSdkRepository,
    private val latestVersionRepo: LatestVersionRepository,
    private val sensorUpgradeRepo: SensorUpgradeRepo,
    private val tempPrefsRepository: TempPrefsRepository,
    private val user: LisMoveUser,
): ViewModel(), KoinComponent {
    val messageObservable = MutableLiveData<String>()
    val sensorNotFoundObservable = MutableLiveData<String>()
    val sensorUpdateObservable = MutableLiveData<String>()
    val sensorLowBatteryEvent = MutableLiveData<Boolean>()
    val warnAboutVersionEvent = MutableLiveData<Int>()
    val requireNewVersionEvent = MutableLiveData<Int>()

    var hasAlreadyShownDfuSensor = false

    init {
        Timber.d("Checked $alertChecked")
        checkIfLatestVersion()
    }

    fun getNotificationIdIfNotificationClicked(intent: Intent): String?{
        return intent.getStringExtra(NotificationMessageDelivery.NOTIFICATION_ID_KEY)
    }

    suspend fun showSensorConfigAlert(): Boolean{
        val bool = !isSensorConfigured() && !alertChecked
        tempPrefsRepository.setConfigSent(true)

        alertChecked = true
        return bool
    }

    private suspend fun isSensorConfigured(): Boolean{
        return sensorRepository.getSensor(user.uid) != null
    }

    suspend fun hasActiveSessionRunning() = sessionSdkRepository.getActiveSession() != null

    fun ensureConnectedToSensor(ctx: Activity) = viewModelScope.launch {
        // Only check if sensor is nearby if there isn't an active session
        val activeSession = sessionSdkRepository.getActiveSession()

        if (activeSession == null) {
            if (!sensorSdk.isConnectedToSensor()) {
                val configuredSensor = sensorRepository.getSensor(user.uid)

                if (configuredSensor != null) {
                    try {
                        delay(1000L)
                        sensorSdk.scanAndConnectToSensorIfNecessary(configuredSensor.uuid,
                            (configuredSensor.wheelDiameter * PI).toFloat(), configuredSensor.hubCoefficient)

                        if (sensorSdk.hasLowBattery()) {
                            sendSensorLowBatteryEvent()
                        }
                    } catch (e: Exception) {
                        showDeviceNotDetectedError()
                    }
                }
            }
        } else {
            // session active, ensure a worker is assigned to it
            if (!sensorSdk.isServiceRunning(ctx)){
                // sensorSdk.start()

                val configuredSensor: SensorEntity? = sensorRepository.getSensor(user.uid)

                if (configuredSensor != null) {
                    sensorSdk.start(
                        configuredSensor,
                        activeSession.id,
                        activeSession.userId,
                        ctx)
                } else {
                    // TODO Try to save and send to server
                    sessionSdkRepository.deleteSessionData(activeSession.id)
                }
            }
        }
    }

    private fun updateFirmwareToServerIfNecessary() {
        Log.e("firmwareUpdate", "Uploading firmware info")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                sensorRepository.updateFirmwareToServerIfNecessary(
                    user.uid, sensorSdk.getSensorFirmwareVersion())
            } catch (e: Exception) {
                BugsnagUtils.reportIssue(e)
            }
        }.runCatching {  }
    }

    private fun upgradeSensorIfNecessary(sensorMacAddress: String) {
        if (!sensorSdk.hasLatestFirmware()) {
            sendSensorUpdateEvent(sensorMacAddress)
        }
    }

    private fun showDeviceNotDetectedError() {
        sensorNotFoundObservable.postValue("Nessun Lis Move rilevato nelle vicinanze")
    }

    private fun sendSensorUpdateEvent(sensorMacAddress: String) {
        sensorUpdateObservable.postValue(sensorMacAddress)
    }

    fun sensorInDfuObservable(): LiveData<String?> {
        return sensorSdk.getSensorInDfuObservable()
    }

    private fun sendSensorLowBatteryEvent() {
        sensorLowBatteryEvent.postValue(true)
    }

    fun startSensorUpgrade(sensorMacAddress: String, ctx: Activity) {
        ctx.startActivity(DeviceUpgradeActivity.getIntent(sensorMacAddress, ctx))
        ctx.finish()
    }

    fun startSensorUpgradeWithoutMac(ctx: Activity) = viewModelScope.launch {
        try {
            val lastSensor: SensorEntity? = sensorRepository.getSensor(user.uid)

            ctx.startActivity(DeviceUpgradeActivity.getIntent(ctx, sensorName = lastSensor?.name))
            ctx.finish()
        } catch (e: Exception){}
    }

    fun observeBleStatus() = viewModelScope.launch {
        sensorSdk.observeBleStatus().collect {
            when (it) {
                LisMoveBleState.BLE_READY -> {
                    GlobalScope.launch {
                        try {
                            updateFirmwareToServerIfNecessary()

                            // Check if sensor is updated
                            val sensorMacAddress = sensorRepository.getSensor(user.uid)?.uuid
                            if (sensorMacAddress != null) {
                                upgradeSensorIfNecessary(sensorMacAddress)
                            }

                            showMessage("Connesso a LisMove")

                            sensorRepository.setSensorFirstPairingDone()
                        } catch (e: Exception) {
                            BugsnagUtils.reportIssue(e)
                        }
                    }
                }
                LisMoveBleState.BLE_CONNECTING -> { }
                LisMoveBleState.BLE_CONNECTED -> { }
                LisMoveBleState.BLE_DISCONNECTING -> { }
                LisMoveBleState.BLE_DISCONNECTED -> {
                    val sensor = sensorRepository.getSensor(user.uid)

                    if (sensor != null) {
                        val sensorMacAddress = sensor.uuid
                        val sensorWheel = sensor.wheelDiameter.toFloat()

                        sensorSdk.scanAndConnectToSensorIfNecessary(sensorMacAddress, sensorWheel,  sensor.hubCoefficient)
                    }
                }
                LisMoveBleState.BLE_FAILED_TO_CONNECT -> { }
            }
        }
    }

    fun observeOfflineHistory() = viewModelScope.launch {
        sensorSdk.observeSensorHistory().collect {
            try {
                if (it.isNotEmpty()) {
                    sensorHistoryRepository.sendSessionHistory(it, user.uid, sensorRepository.getSensor(user.uid)?.uuid)
                    showMessage("Recuperate correttamente ${it.size} sessioni offline")
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun sendNotUploadedSessions(){
        Timber.d("sendNotUploadedSessions")

        viewModelScope.launch {
            sessionCachingUseCase.sendNotUploadedSessions(user.uid)?.let {
                showMessage(it)
            }
        }
    }

    fun startBackgroundDetection(context: Context) {
        SensorDetectionManager.startNearbyDetection(context)
    }

    private fun checkIfLatestVersion() = viewModelScope.launch {
        val latestPublishedVersion = latestVersionRepo.getLatestVersion()
        val latestRequiredVersion = latestVersionRepo.getLatestVersionRequired()

        if (latestRequiredVersion > BuildConfig.VERSION_CODE) {
            requireNewVersionEvent.value = latestRequiredVersion
            return@launch
        }

        if (latestPublishedVersion > BuildConfig.VERSION_CODE) {
            warnAboutVersionEvent.value = latestPublishedVersion
        }
    }

    suspend fun willForceSensorUpdate() = sensorUpgradeRepo.willForceSensorUpdate()

    private fun showMessage(message: String) {
        messageObservable.postValue(message)
    }

    fun getTheme() = themeRepository.getTheme()
}