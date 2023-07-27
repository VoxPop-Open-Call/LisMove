package net.nextome.lismove_sdk.sensorUpgrade

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import net.nextome.lismove_sdk.databinding.ActivityDeviceUpgradeBinding
import net.nextome.lismove_sdk.utils.BugsnagUtils
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import org.koin.android.ext.android.inject

class DeviceUpgradeActivity : AppCompatActivity() {

    val viewModel: DeviceUpgradeViewModel by inject()

    private lateinit var binding: ActivityDeviceUpgradeBinding

    companion object {
        val EXTRA_MAC_ADDRESS = "extra_mac_address"
        val EXTRA_SENSOR_NAME = "extra_sensor_name"

        /**
         * This will update the target device with latest firmware available.
         * Doesn't check if the latest firmware is already installer (check it before calling)
         */
        fun getIntent(targetDeviceMac: String, ctx: Context) = Intent(ctx, DeviceUpgradeActivity::class.java).apply {
            putExtra(EXTRA_MAC_ADDRESS, targetDeviceMac)
        }

        /**
         * This will try and update only devices that are already in DFU mode.
         * If nothing is in DFU mode, it will show a "nothing to update" message
         *
         * sensorName is the server name of the sensor (Lis Move k3 or Lis Move k2...)
         */
        fun getIntent(ctx: Context, sensorName: String?) = Intent(ctx, DeviceUpgradeActivity::class.java).apply {
            sensorName?.let{
                putExtra(EXTRA_SENSOR_NAME, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Aggiornamento sensore..."
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityDeviceUpgradeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            viewModel.parseDataFromIntent(intent)
        } catch (e: Exception){
            Toast.makeText(this, "Si è verificato un errore inaspettato, riprova più tardi", Toast.LENGTH_SHORT).show()
        }

        viewModel.startUpdate(viewModel.targetMacAddress, this)
        viewModel.resultObservable.observe(this) {
            when (it.status) {
                DeviceUpgradeViewModel.UpgradeStatus.ERROR -> {
                    showErrorMessage(it.errorMessage ?: "Impossibile connettersi al sensore")
                    BugsnagUtils.reportIssue(Exception("Sensor upgrade error: unable to connect to sensor."))
                }

                DeviceUpgradeViewModel.UpgradeStatus.NO_UPDATES -> {
                    showSuccessAndRestart("Nessun nuovo aggiornamento")
                }
            }
        }
    }

    fun showErrorMessage(message: String){
        with (binding) {
            messageTextView.text = message
            imageError.visibility = View.VISIBLE
            progressUpdate.visibility = View.GONE
        }
    }

    fun showProgress(percent: Int) {
        with (binding.progressTextView) {
            visibility = View.VISIBLE
            text = "$percent%"
        }
    }

    fun showGenericMessage(message: String) {
        binding.messageTextView.text = message
    }

    fun showSuccessAndRestart(message: String) {
        with (binding) {
            binding.messageTextView.text = message
            imageDone.visibility = View.VISIBLE
            progressUpdate.visibility = View.GONE
            progressTextView.visibility = View.GONE
        }

        viewModel.restartApplicationAfter(5000L, this)
    }

    override fun onPause() {
        DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
    }

    val dfuProgressListener = object: DfuProgressListenerAdapter(){
        override fun onDeviceConnecting(deviceAddress: String) {
            super.onDeviceConnecting(deviceAddress)
            showGenericMessage("Connessione al dispositivo...")
        }

        override fun onDeviceConnected(deviceAddress: String) {
            super.onDeviceConnected(deviceAddress)
            showGenericMessage("Dispositivo connesso")
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            super.onDfuProcessStarting(deviceAddress)
            showGenericMessage("Preparazione all'aggiornamento...")
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            super.onDfuProcessStarted(deviceAddress)
            showGenericMessage("Aggiornamento in corso...")
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            super.onEnablingDfuMode(deviceAddress)
            showGenericMessage("Preparazione all'invio dell'aggiornamento")
        }

        override fun onProgressChanged(
            deviceAddress: String,
            percent: Int,
            speed: Float,
            avgSpeed: Float,
            currentPart: Int,
            partsTotal: Int
        ) {
            super.onProgressChanged(deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal)
            showProgress(percent)
        }

        override fun onFirmwareValidating(deviceAddress: String) {
            super.onFirmwareValidating(deviceAddress)
            showGenericMessage("Validazione del Firmware...")
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            super.onDeviceDisconnecting(deviceAddress)
            showGenericMessage("Disconnessione dal dispositivo...")
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            super.onDeviceDisconnected(deviceAddress)
            showGenericMessage("Dispositivo disconnesso...")
        }

        override fun onDfuCompleted(deviceAddress: String) {
            super.onDfuCompleted(deviceAddress)
            showSuccessAndRestart("Fatto!\nAttendi qualche istante il riavvio del sensore.")
        }

        override fun onDfuAborted(deviceAddress: String) {
            super.onDfuAborted(deviceAddress)
            showErrorMessage("Aggiornamento del sensore rifiutato")
            BugsnagUtils.reportIssue(Exception("Sensor DFU aborted (device address: $deviceAddress"))
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            super.onError(deviceAddress, error, errorType, message)
            showErrorMessage(message ?: "Si è verificato un errore, riprova più tardi")
            BugsnagUtils.reportIssue(Exception("Sensor DFU error ($error $errorType message: $message)"))
        }
    }

    override fun onDestroy() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroy()
    }
}