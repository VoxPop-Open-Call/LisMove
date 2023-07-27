package net.nextome.lismove_sdk.sensorUpgrade

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugsnag.android.Bugsnag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.nextome.lismove_sdk.LismoveBleManager.Companion.lismove_K3_SENSOR_NAME
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.R
import net.nextome.lismove_sdk.utils.BugsnagUtils


class DeviceUpgradeViewModel(
    private val upgradeManager: SensorUpgradeManager,
    private val sensorUpgradeRepo: SensorUpgradeRepo,
    private val sdk: LismoveSensorSdk,
): ViewModel() {

    enum class UpgradeStatus { ERROR, NO_UPDATES }

    data class DeviceUpgradeResult(
        val status: UpgradeStatus,
        val errorMessage: String?,
    )

    var targetMacAddress: String? = null

    // for example, Lis Move k3
    var sensorName: String? = null

    val resultObservable = MutableLiveData<DeviceUpgradeResult>()

    fun startUpdate(targetMacAddress: String?, ctx: Context) = viewModelScope.launch {
        if (targetMacAddress != null) {
            try {
                upgradeManager.updateSensor(targetMacAddress, ctx)
            } catch (e: Exception) {
                BugsnagUtils.reportIssue(Exception("Error during update: ${e.message}"), BugsnagUtils.ErrorSeverity.INFO)
                showError("Impossibile mantenere una connessione con il dispositivo")
            }
        } else {
            val advertisement = withTimeoutOrNull(15000L) {
                upgradeManager.getFirstDeviceInDfu()
            }

            if (advertisement != null) {
                // One device is already in DFU mode
                BugsnagUtils.reportIssue(Exception("Found sensor already in DFU mode"), BugsnagUtils.ErrorSeverity.INFO)
                try {
                    val firmwareName: Int? = if (sensorName == null) { null } else if (sensorName == lismove_K3_SENSOR_NAME) { R.raw.k3_last } else { R.raw.k2_last }
                    BugsnagUtils.reportIssue(Exception("Found sensor already in DFU. Can't detect model, using last used sensor which was $sensorName"))
                    upgradeManager.updateSensorAlreadyInDfu(ctx, firmwareName)
                } catch (e: Exception) {
                    BugsnagUtils.reportIssue(e, BugsnagUtils.ErrorSeverity.ERROR)
                }
            } else {
                // no sensor found to update
                resultObservable.value = DeviceUpgradeResult(UpgradeStatus.NO_UPDATES, "Nessun aggiornamento disponibile")
            }
        }
    }

    private fun showError(message: String) {
        // Propose to skip update at next round
        viewModelScope.launch {
            sensorUpgradeRepo.setForceSensorUpdate(false)
        }

        try {
            upgradeManager.observeBleJob?.cancel()
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
        }

        resultObservable.value = DeviceUpgradeResult(UpgradeStatus.ERROR, message)
    }

    fun parseDataFromIntent(intent: Intent?) {
        val parsedMacAddress = intent?.getStringExtra(DeviceUpgradeActivity.EXTRA_MAC_ADDRESS)
        targetMacAddress = parsedMacAddress

        val parsedSensorName = intent?.getStringExtra(DeviceUpgradeActivity.EXTRA_SENSOR_NAME)
        sensorName = parsedSensorName
    }

    fun restartApplicationAfter(time: Long, context: Context) = viewModelScope.launch {
        delay(time)
        
        context.packageManager.getLaunchIntentForPackage(context.getPackageName())?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(this)
        }
    }
}