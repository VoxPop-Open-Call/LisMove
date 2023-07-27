package net.nextome.lismove_sdk.sensorUpgrade

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RawRes
import androidx.core.content.edit
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import net.nextome.lismove_sdk.LismoveBleManager
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.R
import net.nextome.lismove_sdk.models.LisMoveBleState
import net.nextome.lismove_sdk.utils.BugsnagUtils
import net.nextome.lismove_sdk.utils.LisMoveSensorUtils
import no.nordicsemi.android.dfu.DfuServiceInitiator
import java.lang.StringBuilder
import kotlin.Exception

class SensorUpgradeManager(
    private val bleManager: LismoveBleManager,
    private val sdk: LismoveSensorSdk,
    ctx: Context) {

    var observeBleJob : Job? = null

    suspend fun updateSensor(address: String, ctx: Context) = withContext(Dispatchers.IO) {
        try {
            sdk.scanAndConnectToSensorIfNecessary(address, 1F, 1.0)


            observeBleJob = launch {
                try {
                    sdk.observeBleStatus().collect {
                        if (it == LisMoveBleState.BLE_READY) {
                            val updateFile = when (sdk.sensorHardwareVersion()) {
                                LismoveBleManager.lismove_K2_HARDWARE_VERSION -> R.raw.k2_last
                                else -> R.raw.k3_last
                            }

                            try {
                                if (sdk.getSensorFirmwareVersion() == "V0.6") {
                                    BugsnagUtils.reportIssue(
                                        Exception("Starting updating Firmware 0.6"),
                                        BugsnagUtils.ErrorSeverity.INFO
                                    )
                                }
                            } catch (e: Exception) {

                            }

                            bleManager.sendUpdateFirmwareCommand()

                            updateSensorAlreadyInDfu(ctx, updateFile)
                            observeBleJob?.cancel()
                        }
                    }
                } catch (e: Exception){
                    BugsnagUtils.reportIssue(e)
                }
            }
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            // BugsnagUtils.reportIssue(e, BugsnagUtils.ErrorSeverity.ERROR)
        }
    }

    // BK463_OTA
    suspend fun getFirstDeviceInDfu(): Advertisement? {
        return Scanner()
            .advertisements
            .firstOrNull { LisMoveSensorUtils.isInDfuMode(it.name)}
    }

    suspend fun updateSensorAlreadyInDfu(ctx: Context, @RawRes updateZip: Int? = null) = withContext(Dispatchers.IO) {
        // Scan for devices in DFU mode
        val advertisement = getFirstDeviceInDfu()

        if (advertisement == null) {
            BugsnagUtils.reportIssue(Exception("Sensor not found during DFU. Collecting found sensors."), BugsnagUtils.ErrorSeverity.ERROR)

            val foundSensors = StringBuilder()

            withTimeoutOrNull(10000L) {
                Scanner()
                    .advertisements
                    .collect { advertisement ->
                        foundSensors.append("${advertisement.name}, ")
                    }
            }

            BugsnagUtils.reportIssue(Exception("Sensor not found during DFU. Devices found were: $foundSensors"), BugsnagUtils.ErrorSeverity.ERROR)

            return@withContext
        }

        val dfuTargetMacAddress = advertisement.address
        val dfuTargetName = advertisement.name ?: ""

        val firmwareUpdateZip: Int = if (updateZip == null) {
            // We don't know which sensor model to update.
            // Try k2 first and then k3
            val LAST_MODEL_UPDATED_KEY = "last_model_updated"
            val MODEL_K2 = "k2"
            val MODEL_K3 = "k3"

            // we don't know which device model we are upgrading
            // try first k3 and then k2
            val sharedPref: SharedPreferences = ctx.getSharedPreferences("pin_bike_shared_prefs", 0)

            // get if has already updated a model last time
            val lastModelUpdated = sharedPref.getString(LAST_MODEL_UPDATED_KEY, MODEL_K3)

            val modelToUpdate = if (lastModelUpdated == MODEL_K3) { MODEL_K2 } else { MODEL_K3 }

            sharedPref.edit {
                putString(LAST_MODEL_UPDATED_KEY, modelToUpdate)
            }

            if (modelToUpdate == MODEL_K3) {
                R.raw.k3_last
            } else {
                R.raw.k2_last
            }.also {
                BugsnagUtils.reportIssue(Exception("Tried to update firmware without a given model. Trying ${modelToUpdate} now"))

            }

        } else {
            // We already know the exact model to update
            updateZip
        }

        // upload file
        val starter = DfuServiceInitiator(dfuTargetMacAddress)
            .setDeviceName(dfuTargetName)
            .setKeepBond(true)
            .setPrepareDataObjectDelay(300L)
            .setZip(firmwareUpdateZip)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(ctx)
        }

        val controller = starter.start(ctx, LismoveDfuService::class.java)
    }
}