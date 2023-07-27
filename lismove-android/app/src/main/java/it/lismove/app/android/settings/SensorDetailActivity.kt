package it.lismove.app.android.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import cn.pedant.SweetAlert.SweetAlertDialog
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivitySensorDetailBinding
import it.lismove.app.android.deviceConfiguration.DeviceConfigActivity
import it.lismove.app.android.deviceConfiguration.WheelUtils
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.*
import it.lismove.app.room.entity.SensorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.nextome.lismove_sdk.sensorUpgrade.DeviceUpgradeActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class SensorDetailActivity : LisMoveBaseActivity(), LceView<SensorEntity?> {
    lateinit var binding: ActivitySensorDetailBinding
    val viewModel: SensorDetailViewModel by inject()
    var sensorData: LiveData<Lce<SensorEntity?>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            configureButton.setOnClickListener {
                viewModel.viewModelScope.launch {
                    if (!viewModel.isSessionInProgress()) {
                        openDeviceConfigActivity()
                    } else {
                        Toast.makeText(this@SensorDetailActivity, R.string.error_session_in_progress, Toast.LENGTH_LONG).show()
                    }
                }
            }

            checkUpdatesButton.setOnClickListener {
                viewModel.viewModelScope.launch {
                    if (!viewModel.isSessionInProgress()) {
                        openUpdatesActivity()
                    } else {
                        Toast.makeText(this@SensorDetailActivity, R.string.error_session_in_progress, Toast.LENGTH_LONG).show()
                    }
                }
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            loadingBar.isIndeterminate = true
            loadSensorData()
        }
    }

    private fun openUpdatesActivity() {
        startActivity(DeviceUpgradeActivity.getIntent(this, sensorName = viewModel.sensor?.name))
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadSensorData()
    }

    private fun loadSensorData(){
        sensorData?.removeObservers(this)
        sensorData = viewModel.getSensorData()
        sensorData?.observe(this, LceDispatcher(this))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun openDeviceConfigActivity(){
        startActivity(Intent(this, DeviceConfigActivity::class.java))
        finish()
    }

    override fun onLoading() {
        with(binding){
            loadingBar.visibility = View.VISIBLE
            readyGroup.visibility = View.GONE
        }
    }

    override fun onSuccess(data: SensorEntity?) {
        binding.loadingBar.visibility = View.GONE
        if(data != null){
            showSensorUI(data)
        }else{
            showEmptySensorUI()
        }

    }

    override fun onError(throwable: Throwable) {
        showError(throwable.localizedMessage ?: "Si è verificato un errore", binding.root )
    }

    fun askConfirmationToSetSensorStolen(sensorUid: String){
        showConfirmationAlertDialog("", "Sei sicuro di voler segnalare il furto"){
            setSensorStolen(sensorUid)
        }

    }

    fun setSensorStolen(sensorUid: String){
        val dialog = SweetAlertDialog(this).apply {
            setTitleText("Caricamento")
            changeAlertType(SweetAlertDialog.PROGRESS_TYPE)
            show()
        }
        viewModel.setSensorStolen(sensorUid).observe(this){ data ->
            when(data){
                is LceLoading -> {
                    dialog.show()
                }
                is LceSuccess ->{
                    with(dialog){
                        setTitleText("Operazioni effettuata con successo")
                        setConfirmText("Contintua")
                        setConfirmClickListener {
                            it.dismissWithAnimation()
                        }
                        onSuccess(data.data)
                        dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    }
                }

                is LceError -> {
                    with(dialog){
                        setTitleText("Si è verificato un errore")
                        setContentText(data.error.message)
                        changeAlertType(SweetAlertDialog.ERROR_TYPE)
                    }
                }
            }
        }
    }
    private fun showSensorUI(sensor: SensorEntity){
        with(binding){
            configureButton.text = "Riconfigura la tua bicicletta"
            sensorLayout.visibility = View.VISIBLE
            k2UIDLabel.text = sensor.name
            k2FirmwareValue.text = sensor.firmware ?: "Informazione sul firwmare non disponibile"
            if(sensor.wheelDiameter != 0){
                k2Wheel.text = WheelUtils.getWheelString(sensor.wheelDiameter)
            }else{
                k2Wheel.text = "0"

            }
            configureButton.visibility = View.VISIBLE
            devicedNotConfiguredGroup.visibility = View.GONE
            hubCoefficientLayout.isVisible = false
            hubCoefficientLabel.text = "${sensor.hubCoefficient}"
            setStoleButton.text = if(sensor.stolen) "FURTO SEGNALATO" else "SEGNALA FURTO"
            setStoleButton.isEnabled = sensor.stolen.not()
            if(sensor.stolen.not()){
                setStoleButton.setOnClickListener {
                    viewModel.viewModelScope.launch {
                        if (viewModel.isSessionInProgress()) {
                            Toast.makeText(
                                this@SensorDetailActivity,
                                viewModel.sensorStolenDuringSessionError,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            askConfirmationToSetSensorStolen(sensor.uuid)
                            Timber.d("setSensorAsStolen")
                        }
                    }
                }

            }
            removeSensor.setOnClickListener {
                viewModel.viewModelScope.launch {
                    if(viewModel.isSessionInProgress()){
                        Toast.makeText(this@SensorDetailActivity, viewModel.sensorDisassociateDuringSessionError, Toast.LENGTH_SHORT).show()
                    }else{
                        showDisassociateAlert()
                    }
                }
            }
        }
    }

    fun showEmptySensorUI(){
        with(binding){
            configureButton.text = "Configura la tua bicicletta"
            sensorLayout.visibility = View.GONE
            configureButton.visibility = View.VISIBLE
            devicedNotConfiguredGroup.visibility = View.VISIBLE
        }
    }

    private fun showDisassociateAlert(){
        showConfirmationAlertDialog("", "Sei sicuro di voler disassociare il sensore?"){
            disassociateSensor()
        }
    }

    private fun disassociateSensor(){
        val dialog = SweetAlertDialog(this@SensorDetailActivity).apply {
            setTitleText("Caricamento")
            changeAlertType(SweetAlertDialog.PROGRESS_TYPE)
            show()
        }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModel.disassociateSensor()
                withContext(Dispatchers.Main){
                    dialog.apply {
                        setTitleText("Operazioni effettuata con successo")
                        setConfirmText("Contintua")
                        setConfirmClickListener {
                            it.dismissWithAnimation()
                            onSuccess(null)

                        }

                        dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    }
                }

            }catch (e: Exception){
                Timber.d(e.localizedMessage)
                with(dialog){
                    setTitleText("Si è verificato un errore")
                    setContentText(e.message)
                    changeAlertType(cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE)
                }
            }
        }
    }
}