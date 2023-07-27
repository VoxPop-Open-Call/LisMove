package net.nextome.lismove_sdk

import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.google.gson.Gson
import com.juul.kable.*
import it.lismove.app.room.LisMoveDatabase
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.SensorEntity
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.database.*
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_CREATED
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_PAUSE_BY_USER
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_PAUSE_INACTIVITY
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_RUNNING
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_STOPPED
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import net.nextome.lismove_sdk.NotificationProvider.NOTIFICATION_CODE_NEW_DEVICE_DETECTED
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K2_HARDWARE_VERSION
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K2_LASTEST_SOFTWARE_VERSION
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K3_HARDWARE_VERSION
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K3_HARDWARE_VERSION_2
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K3_LATEST_SOFTWARE_VERSION
import net.nextome.lismove_sdk.location.LisMoveLocationManager
import net.nextome.lismove_sdk.models.LisMoveServiceState
import net.nextome.lismove_sdk.models.LisMoveDevice
import net.nextome.lismove_sdk.utils.BluetoothMedic
import net.nextome.lismove_sdk.utils.LisMoveSensorUtils
import no.nordicsemi.android.support.v18.scanner.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


const val BACKGROUND_SCAN_PENDING_INTENT_ID = 666 // it's a ble background daemon ;)
const val BATTERY_LOW_THRESHOLD = 60 // under 60%, people will be notified to change battery

class LismoveSensorSdk: KoinComponent {
    val TAG = "LisMoveSensorSdk"

    private val SENSOR_WORKER_ID = "lismove_sensor_worker"
    private val DEVICE_NAME_LIST = listOf("Cycplus S1", "Lis Move s1", "BK463S-000001", "Lis Move k2", "Lis Move k3")

    private val db: LisMoveDatabase by inject()
    private val sessionRepository: SessionSdkRepository by inject()
    private val locationManager: LisMoveLocationManager by inject()
    private val bleManager: LismoveBleManager by inject()
    private val logRepo: DebugLogRepository by inject()

    private val _detectedSensorInDfu = MutableLiveData<String?>(null)

    suspend fun start(device: SensorEntity, sessionId: String, userId: String, context: Context) {
        Log.d(TAG, "Sdk method start called")
        // remove lismove detected notification, if any
        removeDeviceFoundNotification(context)

        // Create session entry
        sessionRepository.addSessionOrIgnore(
            SessionDataEntity(
            id = sessionId,
            proposedStatus = SESSION_STATUS_CREATED,
            userId = userId,
            // TODO this should be popolated by workmanager at actual start
            startTime = System.currentTimeMillis()
        ))

        Log.d(TAG, "Proposing status \"CREATED\"")


        val sensorWorkerRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SensorWorker>()
            .setInputData(workDataOf(KEY_INPUT_CONFIG to Gson().toJson(WorkerConfiguration(
                device = device,
                sessionId = sessionId,
                userId = userId,
                isResume = false
            ))))
            .addTag(SENSOR_WORKER_ID)
            .build()

        Log.d(TAG, "Started background service")

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(SENSOR_WORKER_ID, ExistingWorkPolicy.REPLACE, sensorWorkerRequest)
    }

    fun isConnectedToSensor() = bleManager.isConnected
    fun disconnectFromSensor() {
        try {
            bleManager.disconnect().await()
            bleManager.close()
        } catch (e: Exception) {

        }
    }

    /** Ble scan **/

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .build()

    private val filters: MutableList<ScanFilter> = ArrayList()
    private var targetDeviceMac: String? = null
    private var targetCircunference: Float = 736f
    private var hubCoefficient: Double = 1.0

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (targetDeviceMac != null) {

                // If already connected, stop scanning
                if (isConnectedToSensor()) {
                    logRepo.addBleLogAsync("Ble is connected, stopped scanning")
                    stopScanning()
                }

                logRepo.addBleLogAsync("Ble scan result: (${result.device.name}, ${result.device.address})")

                if (result.device.address == targetDeviceMac) {
                    GlobalScope.launch(Dispatchers.IO) {
                        if (!isConnecting) {
                            logRepo.addBleLogAsync("Connecting to sensor")
                            connectToSensor(result.device, targetCircunference, hubCoefficient)
                        } else {
                            logRepo.addBleLogAsync("Is already connecting")
                        }
                    }
                } else if (LisMoveSensorUtils.isInDfuMode(result.device.name)) {
                    // report detected dfu device
                    _detectedSensorInDfu.value = result.device.name
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)

            // If already connected, stop scanning
            if (isConnectedToSensor()) {
                logRepo.addBleLogAsync("Ble is connected, stopped scanning")
                stopScanning()
            }

            if (targetDeviceMac != null) {
                results.forEach {
                    Log.e("SensorSdk", "Found ${it.device.name}")

                    logRepo.addBleLogAsync("Ble scan result: (${it.device.name}, ${it.device.address})")

                    if (it.device.address == targetDeviceMac) {
                        GlobalScope.launch(Dispatchers.IO) {
                            if (!isConnecting) {
                                logRepo.addBleLogAsync("Connecting to sensor")
                                connectToSensor(it.device, targetCircunference, hubCoefficient)
                            } else {
                                logRepo.addBleLogAsync("Is already connecting")
                            }
                        }
                    } else if (LisMoveSensorUtils.isInDfuMode(it.device.name)) {
                        // report detected dfu device
                        _detectedSensorInDfu.value = it.device.name
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            BluetoothMedic.getInstance().resetBluetooth()
            logRepo.addBleLogAsync("ScanCallback: Scan failed with errorCode $errorCode")

            super.onScanFailed(errorCode)
        }
    }


    var lastScanTimestamp: Long = 0
    var isScanRunning = false
    var isConnecting = false

    /**
     * This accepts new scans only only 10 seconds
     * If a new scan is requested < 10 seconds it will be ignored
     */
    fun scanAndConnectToSensorIfNecessary(macAddress: String, wheelCircunference: Float, hubCoefficient: Double) {
        isConnecting = false

        val currentScanTimestamp = System.currentTimeMillis()

        if (isScanRunning) {
            if ((currentScanTimestamp - lastScanTimestamp) < 10000) {
                Log.e("SensorSdk", "Scan already performing")
                return
            }
        }

        lastScanTimestamp = currentScanTimestamp

        stopScanning()

        targetDeviceMac = macAddress
        targetCircunference = wheelCircunference
        this.hubCoefficient = hubCoefficient

        with(bleManager) {
            if (isConnected) {
                setWheelCircunferenceInMm(wheelCircunference)
                setHubCoefficient(hubCoefficient)
                return@with
            }

            setWheelCircunferenceInMm(wheelCircunference)
            setHubCoefficient(hubCoefficient)

            try {
                // Scan before connecting
                isScanRunning = true
                scanner.startScan(filters, settings, scanCallback)
            } catch (e: Exception) {
                BluetoothMedic.getInstance().resetBluetooth()
                logRepo.addBleLogAsync("scanAndConnectToSensorIfNecessary error:" + e.localizedMessage)
                // BugsnagUtils.reportIssue(e, BugsnagUtils.ErrorSeverity.INFO)
            }
        }
    }

    private suspend fun connectToSensor(device: BluetoothDevice, wheelCircunference: Float, hubCoefficient: Double) {
        isConnecting = true

        Log.e("SensorSdk", "Connecting to sensor")

        try {
            val connectionRequest = bleManager.connect(device)
                .timeout(5000)
                .retry(0)
                .useAutoConnect(true)
                .fail { device, status ->
                    logRepo.addBleLogAsync("connectToSensor fail: BLE status: $status on device $device")
                    Log.e("SensorSdk", "Connection Failed")
                    Log.e("LismoveBle", "BLE status: $status on device $device")

                    bleManager.disconnect()
                    bleManager.close()

                    isConnecting = false

                    // scan for updates in nearby devices when connection fails
                    if (targetDeviceMac != null) {
                        scanAndConnectToSensorIfNecessary(targetDeviceMac!!, wheelCircunference, hubCoefficient)
                    }
                }
                .done {
                    Log.e("SensorSdk", "Done")
                    Log.i(TAG, "Device initiated: ${it.name}")

                    logRepo.addBleLogAsync("connectToSensor done on device $device")

                    isScanRunning = false
                    isConnecting = false
                }

            connectionRequest.await()
        } catch (e: Exception) {
            Log.e("SensorSdk", "Failed with ${e.localizedMessage}")
            logRepo.addBleLogAsync("connectToSensor error with ${e.localizedMessage}")

            isConnecting = false
            delay(1000)
            scanAndConnectToSensorIfNecessary(device.address, wheelCircunference, hubCoefficient)
        }
    }

    private fun stopScanning() {
        isScanRunning = false
        scanner.stopScan(scanCallback)
    }

    fun lismoveDeviceToBleDevice(device: SensorEntity, ctx: Context): BluetoothDevice? {
        try {
            val bleMan = (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)
            val bleDevice: BluetoothDevice? = bleMan?.adapter?.getRemoteDevice(device.uuid)

            return bleDevice
        } catch (e: Exception) {
            return null
        }
    }

    fun macAddressToBleDevice(macAddress: String, ctx: Context): BluetoothDevice? {
        try {
            val bleMan = (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)

            return bleMan?.adapter?.getRemoteDevice(macAddress)
        } catch (e: Exception) {
            return null
        }
    }

    private fun removeDeviceFoundNotification(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(NOTIFICATION_CODE_NEW_DEVICE_DETECTED)
    }


    fun observeSensorHistory() = bleManager.offlineHistoryObservable
    fun observeServiceStatus(ctx: Context): LiveData<LisMoveServiceState?> = liveData {
        val sensorLiveData = WorkManager.getInstance(ctx).getWorkInfosForUniqueWorkLiveData(SENSOR_WORKER_ID)

        emitSource(sensorLiveData.map {
            val workInfo = it.firstOrNull()

            if (workInfo != null) {
                val connectionStatusInfo = workInfo.progress.getString(KEY_OUTPUT_WORKER_STATUS)
                val connectionStatus = Gson().fromJson(connectionStatusInfo, LisMoveServiceState::class.java)
                connectionStatus
            } else {
                null
            }
        })
    }

    fun observeSensorData(ctx: Context): LiveData<PartialSessionDataEntity?> = liveData {
        val sensorLiveData = WorkManager.getInstance(ctx).getWorkInfosForUniqueWorkLiveData(SENSOR_WORKER_ID)
        emitSource(sensorLiveData.map {
            val workInfo = it.firstOrNull()

            if (workInfo != null) {
                val partialSerialized = workInfo.progress.getString(KEY_OUTPUT_LAST_DATA)
                val partial = Gson().fromJson(partialSerialized, PartialSessionDataEntity::class.java)
                partial
            } else {
                null
            }
        })
    }

    suspend fun getLastLocation(applicationContext: Context) = locationManager.awaitLastLocationOrNull(applicationContext)

    fun observeBleStatus() = bleManager.observeConnectionStatus()

    fun sensorHardwareVersion() = bleManager.getHardwareRevision()

    suspend fun hasLowBattery(): Boolean {
        val batteryValue = bleManager.batteryObservable.first()
        return batteryValue < BATTERY_LOW_THRESHOLD
    }

    fun hasLatestFirmware(): Boolean {
        val installedVersion = bleManager.getSoftwareRevision()
        val hardwareVersion = bleManager.getHardwareRevision()

        if (installedVersion.isBlank()) return true
        if (hardwareVersion.isBlank()) return true

        Log.d("LisMoveSensor", "Firmware is ${installedVersion}. Hardware revision is ${hardwareVersion}")

        // fix for 0.6 firmware
        if (installedVersion == "V0.6") {
            // BugsnagUtils.reportIssue(Exception("Proposed update Firmware V0.6"), BugsnagUtils.ErrorSeverity.INFO)
            return false
        }

        when (hardwareVersion) {
            lismove_K2_HARDWARE_VERSION -> {
                return installedVersion == lismove_K2_LASTEST_SOFTWARE_VERSION
            }
            lismove_K3_HARDWARE_VERSION -> {
                return installedVersion == lismove_K3_LATEST_SOFTWARE_VERSION
            }
            lismove_K3_HARDWARE_VERSION_2 -> {
                return installedVersion == lismove_K3_LATEST_SOFTWARE_VERSION
            }

            else -> return true
        }
    }

    fun getSensorFirmwareVersion() = bleManager.getSoftwareRevision()

    suspend fun pause(sessionId: String, isForcedByUser: Boolean) {
        Log.d(TAG, "pause called")

        if (isForcedByUser) {
            sessionRepository.updateProposedStatus(
                sessionId,
                SESSION_STATUS_PAUSE_BY_USER,
            )
        } else {
            sessionRepository.updateProposedStatus(
                sessionId,
                SESSION_STATUS_PAUSE_INACTIVITY,
            )
        }
    }

    suspend fun resume(sessionId: String) {
        Log.d(TAG, "resume called")
        sessionRepository.updateProposedStatus(
                sessionId,
                SESSION_STATUS_RUNNING,
        )
    }

    suspend fun stop(sessionId: String) {
        Log.d(TAG, "stop called")
        sessionRepository.updateProposedStatus(
                sessionId,
                SESSION_STATUS_STOPPED,
        )
    }
    fun clearCurrentWork(ctx: Context){
        WorkManager.getInstance(ctx).cancelUniqueWork(SENSOR_WORKER_ID)
    }

    private val MAX_WAIT_TIME_MILLIS = 30000L

    suspend fun scanAndReturnDevice(macAddress: String? = null, timeout: Long = MAX_WAIT_TIME_MILLIS) = withContext(Dispatchers.IO) {
        withTimeoutOrNull(timeout) {
            if (bleManager.isConnected) {
                bleManager.disconnect().await()
            }

            var sensorName = ""
            var sensorMac = ""

            val advertisement = if (macAddress == null) {
                Scanner()
                    .advertisements
                    .first { DEVICE_NAME_LIST.contains(it.name) }
            } else {
                Scanner()
                    .advertisements
                    .first { it.address == macAddress }
            }

            sensorName = advertisement.name ?: ""
            sensorMac = advertisement.address

/*
            // Read firmware
            var firmwareVersion = ""
            var peripheral: Peripheral? = null
            peripheral = peripheral(advertisement)
            peripheral.connect()

            val firmwareVersionBytes: ByteArray = peripheral.read(
                characteristicOf(
                    service = SERVICE_INFO_UUID.toString(),
                    characteristic = SOFTWARE_VERSION_READ_UUID.toString()
                )
            )

            firmwareVersion = firmwareVersionBytes.decodeToString()

            withTimeoutOrNull(5_000L) {
                peripheral.disconnect()
            }
*/

            val device = LisMoveDevice(sensorName, sensorMac)

            return@withTimeoutOrNull device
        }
    }

    suspend fun isServiceRunning(ctx: Context): Boolean {
        val workerInfoFuture = WorkManager.getInstance(ctx).getWorkInfosForUniqueWork(SENSOR_WORKER_ID)
        var running = false

        try {
            val workerInfo = workerInfoFuture.await()

            workerInfo.forEach {
                running = running || (it.state === WorkInfo.State.RUNNING) or (it.state == WorkInfo.State.ENQUEUED)
            }

        } catch (e: Exception) {
            return false
        }

        Log.e("testtest", "Service runnin is $running")
        return running
    }

    /**
     * Returns if sdk found a sensor that is in DFU mode, with its name
     */
    fun getSensorInDfuObservable(): LiveData<String?> {
        return _detectedSensorInDfu.distinctUntilChanged()
    }

    fun refreshBackgroundSensorDetection(context: Context) {
/*        bleIntent = Intent(context, NewDeviceFoundReceiver::class.java).apply {
            action = "net.nextome.lismove_sdk.DEVICE_FOUND"
        }

        blePendingIntent =
            PendingIntent.getBroadcast(context, BACKGROUND_SCAN_PENDING_INTENT_ID,
                bleIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(10000)
            .build()

        val filters: MutableList<ScanFilter> = ArrayList()

        DEVICE_NAME_LIST.forEach {
            filters.add(ScanFilter.Builder().setDeviceName(it).build())
        }

        stopScanningService(context)
        blePendingIntent?.let {
            try {
                scanner.startScan(filters, settings, context, it)
                Log.e("lismovetest", "Background scan started")
            } catch (e: Exception){
                Log.e("lismovetest", "Bluetooth is not enabled")
            }
        }*/
    }


/*    fun stopScanningService(context: Context) {
        blePendingIntent?.let{
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(context, it)
        }
    }*/
}



