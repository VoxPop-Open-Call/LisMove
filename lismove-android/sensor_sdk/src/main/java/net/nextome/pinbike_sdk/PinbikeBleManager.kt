package net.nextome.lismove_sdk

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import net.nextome.lismove_sdk.models.*
import net.nextome.lismove_sdk.utils.SensorDataManager.Companion.calculateDistanceInKm
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.annotation.ConnectionState
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementDataCallback
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*
import kotlin.math.PI


class LismoveBleManager(context: Context) : BleManager(context) {
    private var wheelCircumferenceInMm: Float = 0F
    private var hubCoefficient: Double = 1.0
    // Client characteristics
    private var lismoveChar: BluetoothGattCharacteristic? = null
    private var batteryChar: BluetoothGattCharacteristic? = null
    private var softwareVersionChar: BluetoothGattCharacteristic? = null
    private var hardwareVersionChar: BluetoothGattCharacteristic? = null
    private var firmwareVersionChar: BluetoothGattCharacteristic? = null
    private var offlineHistoryChar: BluetoothGattCharacteristic? = null
    private var controlCharWrite: BluetoothGattCharacteristic? = null
    private var isV2 = false
    private var softwareRevision = ""
    private var hardwareRevision = ""
    private var firmwareRevision = ""

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    override fun log(priority: Int, message: String) {
        // Log.e("LismoveBleManager", message)
    }


    private val cadenceEmitter = MutableStateFlow<CadencePrimitive?>(null)
    val cadenceObserver = cadenceEmitter.filterNotNull()
    private val batteryEmitter = MutableStateFlow<Int?>(null)
    val batteryObservable = batteryEmitter.filterNotNull()
    private val offlineHistoryEmitter = MutableStateFlow<List<LisMoveSensorHistoryElement>>(listOf())
    val offlineHistoryObservable = offlineHistoryEmitter.filterNotNull()


    fun getSoftwareRevision() = softwareRevision // This is the one sent to server in session
    fun getHardwareRevision() = hardwareRevision
    fun getFirmwareRevision() = firmwareRevision

    fun setWheelCircunferenceInMm(value: Float) {
        wheelCircumferenceInMm = value
    }

    fun setHubCoefficient(value: Double){
        hubCoefficient = value
    }

    fun setWheelDiameter(value: Float) {
        wheelCircumferenceInMm = (value * PI).toFloat()
    }

    private val connectionFlow = MutableStateFlow<LisMoveBleState?>(null)

    private val lismoveConnectionObserver = object: ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {
            connectionFlow.value = LisMoveBleState.BLE_CONNECTING
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            connectionFlow.value = LisMoveBleState.BLE_CONNECTED
        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
            connectionFlow.value = LisMoveBleState.BLE_FAILED_TO_CONNECT
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            connectionFlow.value = LisMoveBleState.BLE_READY
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            connectionFlow.value = LisMoveBleState.BLE_DISCONNECTING
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            connectionFlow.value = LisMoveBleState.BLE_DISCONNECTED
        }
    }

    fun observeConnectionStatus() = connectionFlow
        .asStateFlow()
        .onStart {
            when (connectionState) {
                BluetoothProfile.STATE_CONNECTING -> {
                    emit(LisMoveBleState.BLE_CONNECTING)
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    emit(LisMoveBleState.BLE_CONNECTED)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    emit(LisMoveBleState.BLE_DISCONNECTED)
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    emit(LisMoveBleState.BLE_DISCONNECTING)
                }
            }
        }
        .filterNotNull()
    suspend fun sendUpdateFirmwareCommand() = suspendCancellableCoroutine<Boolean> { cont ->
        beginAtomicRequestQueue()
            .add(writeCharacteristic(controlCharWrite, getDFUModeCommand()))
            .done { cont.resume(true){} }
            .fail { _, _ -> cont.resume(false){} }
            .enqueue()
    }

    private fun getDFUModeCommand() = byteArrayOfInts(0xA2, 0x04, 0x01, 0xA7)
    private fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }


    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(SERVICE_SESSION_UUID)
            if (service != null) {
                lismoveChar = service.getCharacteristic(CHAR_SESSION_NOTIFY_UUID)
            }

            val serviceV2 = gatt.getService(CONTROL_SERVICE_UUID)
            if (serviceV2 != null) {
                isV2 = true
                offlineHistoryChar = serviceV2.getCharacteristic(CHAR_V2_NOTIFY_UUID)
                controlCharWrite = serviceV2.getCharacteristic(CHAR_CONTROL_WRITE_UUID)
            }

            val batteryService = gatt.getService(SERVICE_BATTERY_UUID)
            if (batteryService != null) {
                batteryChar = batteryService.getCharacteristic(BATTERY_CHAR_NOTIFY_UUID)
            }

            val infoService = gatt.getService(SERVICE_INFO_UUID)
            if (infoService != null) {
                softwareVersionChar = infoService.getCharacteristic(SOFTWARE_REVISION_READ_UUID)
                hardwareVersionChar = infoService.getCharacteristic(HARDWARE_REVISION_READ_UUID)
                firmwareVersionChar = infoService.getCharacteristic(FIRMWARE_REVISION_READ_UUID)
            }

            // Return true if all required services have been found
            return lismoveChar != null
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        override fun initialize() {
            setConnectionObserver(lismoveConnectionObserver)

            readCharacteristic(softwareVersionChar).with { device, data ->
                softwareRevision = data.getStringValue(0) ?: ""
            }.enqueue()

            readCharacteristic(hardwareVersionChar).with { device, data ->
                hardwareRevision = data.getStringValue(0) ?: ""
            }.enqueue()

            readCharacteristic(firmwareVersionChar).with { device, data ->
                firmwareRevision = data.getStringValue(0) ?: ""
            }.enqueue()

            setNotificationCallback(batteryChar).with(object : BatteryLevelDataCallback(){
                override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
                    batteryEmitter.value = batteryLevel
                }
            })

            setNotificationCallback(lismoveChar).with(object: CyclingSpeedAndCadenceMeasurementDataCallback() {
                override fun onWheelMeasurementReceived(device: BluetoothDevice, wheelRevolutions: Long, lastWheelEventTime: Int) {
                    super.onWheelMeasurementReceived(device, wheelRevolutions, lastWheelEventTime)
                    cadenceEmitter.value = CadencePrimitive(wheelRevolutions, lastWheelEventTime)
                }
                override fun getWheelCircumference(): Float { return wheelCircumferenceInMm }

                // Not used
                override fun onDistanceChanged(device: BluetoothDevice, totalDistance: Float, distance: Float, speed: Float) {
                }
                override fun onCrankDataChanged(device: BluetoothDevice, crankCadence: Float, gearRatio: Float) {}
            })

            if (isV2) {
                setNotificationCallback(offlineHistoryChar).with(object : SensorV2DataCallback() {
                    override fun onHistoryReceived(historyData: List<LisMoveSensorHistoryElement>) {
                        offlineHistoryEmitter.value = historyData.map { it.copy(
                            distance = calculateDistanceInKm(it.startLap, it.stopLap, wheelCircumferenceInMm, hubCoefficient)
                        ) }
                    }
                })
            }

            val queue = beginAtomicRequestQueue()
                .add(enableNotifications(lismoveChar))
                .add(enableNotifications(batteryChar))

            if (isV2) {
                queue.add(enableNotifications(offlineHistoryChar))
            }

            queue.done { device: BluetoothDevice? -> log(Log.INFO, "Target initialized") }
                .fail { device, status -> log(Log.ERROR, "Failed") }
                .enqueue()
        }

        private fun getRequestHistoryCommand() = byteArrayOfInts(0xBB, 0x04, 0x08, 0xAA)

        private fun getSetTimestampCommand(): ByteArray {
            fun longToUInt32ByteArray(value: Long): ByteArray {
                val bytes = ByteArray(4)
                bytes[3] = (value and 0xFFFF).toByte()
                bytes[2] = ((value ushr 8) and 0xFFFF).toByte()
                bytes[1] = ((value ushr 16) and 0xFFFF).toByte()
                bytes[0] = ((value ushr 24) and 0xFFFF).toByte()
                return bytes
            }

            val timeInSeconds = System.currentTimeMillis()/1000

            val payloadBytes = byteArrayOfInts(0xBB, 0x08, 0x07)
                .plus(longToUInt32ByteArray(timeInSeconds))
                .plus(byteArrayOfInts(0XAA))

            payloadBytes.map { Log.e("timestamp", it.toString()) }

            return payloadBytes
        }

        fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

        override fun onDeviceReady() {
            super.onDeviceReady()

            beginAtomicRequestQueue()
                .add(writeCharacteristic(controlCharWrite, getSetTimestampCommand())
                    .done { device: BluetoothDevice? ->
                        log(Log.INFO, "DONE")
                        GlobalScope.launch {
                            delay(2000)
                            beginAtomicRequestQueue().add(writeCharacteristic(controlCharWrite, getRequestHistoryCommand())
                                .done { device: BluetoothDevice? ->
                                    log(Log.INFO, "DONE")
                                }
                                .fail { device, status ->
                                    log(Log.INFO, "ERROR")
                                }).enqueue()
                        }
                    }
                    .fail { device, status ->
                        log(Log.INFO, "ERROR")
                    }).enqueue()
        }

        override fun onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            lismoveChar = null
            batteryChar = null
            offlineHistoryChar = null
        }

        override fun onServicesInvalidated() {
            // Device disconnected. Release your references here.
            lismoveChar = null
            batteryChar = null
            offlineHistoryChar = null
        }

        private val lismoveConnectionObserver = object: ConnectionObserver {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                connectionFlow.value = LisMoveBleState.BLE_CONNECTING
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
                connectionFlow.value = LisMoveBleState.BLE_CONNECTED
            }

            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                connectionFlow.value = LisMoveBleState.BLE_FAILED_TO_CONNECT
            }

            override fun onDeviceReady(device: BluetoothDevice) {
                connectionFlow.value = LisMoveBleState.BLE_READY
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                connectionFlow.value = LisMoveBleState.BLE_DISCONNECTING
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                connectionFlow.value = LisMoveBleState.BLE_DISCONNECTED
            }
        }
    }

    companion object {
        val SERVICE_SESSION_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb")
        val CHAR_SESSION_NOTIFY_UUID = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb")

        val CONTROL_SERVICE_UUID = UUID.fromString("0000fd00-0000-1000-8000-00805f9b34fb")
        val CHAR_V2_NOTIFY_UUID = UUID.fromString("0000fd09-0000-1000-8000-00805f9b34fb")
        val CHAR_CONTROL_WRITE_UUID = UUID.fromString("0000fd0a-0000-1000-8000-00805f9b34fb")

        val SERVICE_BATTERY_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        val BATTERY_CHAR_NOTIFY_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

        val SERVICE_INFO_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val SOFTWARE_REVISION_READ_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb") // the one sent to server
        val HARDWARE_REVISION_READ_UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
        val FIRMWARE_REVISION_READ_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")


        const val lismove_K2_HARDWARE_VERSION = "V1.2"
        const val lismove_K2_LASTEST_SOFTWARE_VERSION = "V3.11.0"
        const val lismove_K3_HARDWARE_VERSION = "HW1.0.0S"
        const val lismove_K3_HARDWARE_VERSION_2 = "V1.0.0S"
        const val lismove_K3_LATEST_SOFTWARE_VERSION = "V3.13.7"

        const val lismove_K3_SENSOR_NAME = "Lis Move k3"

        // VERSIONI K2
        // aggiornamento riuscito da 3.13.6 -> 3.11
        // aggiornamento riuscito da 3.13.8 -> 3.11

        // manca immagine 3.6 (vecchia)
        // manca immagine 3.8 (vecchia)


        // VERSIONI K3
        // aggiornamento riuscito da 3.13.4 -> 3.13.5

        // immagine 3.13.3
        // manca immagine 3.13.4
        // manca immagine 3.13.6 (più recente)
    }
}