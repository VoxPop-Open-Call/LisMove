package it.lismove.app.android.deviceConfiguration

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityDeviceConfigurationBinding
import it.lismove.app.android.deviceConfiguration.adapter.DeviceConfigurationAdapter
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.common.LisMovePermissionsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.*
import net.nextome.lismove_sdk.models.LisMoveDevice
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber

private const val PERMISSIONS_REQUEST_CODE = 1

class DeviceConfigActivity : LisMoveBaseActivity(),
    LceView<LisMoveDevice>,
    DeviceConfigCallback,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityDeviceConfigurationBinding

    private val configViewModel: DeviceConfigurationViewModel  by inject()
    private val errorButtonTag = 1
    private val numberOfScreens = 4
    val adapter = DeviceConfigurationAdapter(this, this, numberOfScreens)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_configuration)

        binding = ActivityDeviceConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        configViewModel.disconnectAll()

        setupViewPager()
        setupButtonListener()
        setupEventListener()
        listenForDeviceUpgrade()
        checkIfBluetoothEnabled()
    }

    private fun checkIfBluetoothEnabled() {
        if (!configViewModel.isBluetoothEnabled()) {
            with(AlertDialog.Builder(this@DeviceConfigActivity)) {
                setTitle("Bluetooth disabilitato o non disponibile")
                setMessage("Attiva il bluetooth per poter configurare un nuovo sensore Lis Move.")
                setCancelable(false)
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }

                show()
            }
        }
    }

    private fun listenForDeviceUpgrade() {
        configViewModel.sensorUpdateObservable.observe(this) { macAddress ->
            lifecycleScope.launch {

                val dialogMessage = if (configViewModel.willForceSensorUpdate()) {
                    "Per usufluire degli ultimi miglioramenti, è necessario aggiornare il sensore Lismove ora."
                } else {
                    "E' disponibile un nuovo aggiornamento per il sensore Lis Move. Aggiornare ora?"
                }

                with(AlertDialog.Builder(this@DeviceConfigActivity)) {
                    setTitle("Nuovo aggiornamento disponibile")
                    setMessage(dialogMessage)
                    setCancelable(false)
                    setPositiveButton("Si") { dialog, _ ->
                        configViewModel.startSensorUpgrade(macAddress, this@DeviceConfigActivity)
                        dialog.dismiss()
                    }

                    if (!configViewModel.willForceSensorUpdate()) {
                        setNegativeButton("Più tardi") { dialog, _ ->
                            BugsnagUtils.reportIssue(
                                Exception("Aggiornamento sensore ignorato da utente."),
                                BugsnagUtils.ErrorSeverity.INFO
                            )
                            dialog.dismiss()
                        }
                    }

                    show()
                }
            }
        }
    }

    private fun setupButtonListener(){
        with(binding){
            configClose.setOnClickListener { finish() }
            configButton.setOnClickListener { onConfigButtonClicked() }
        }
    }
    private fun setupViewPager(){
        with(binding){
            configurationPager.adapter = adapter
            configDotIndicator.setViewPager2(configurationPager)
            configurationPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Timber.d("OnPageSelected $position")
                    if(position == 2){ startScanning() }
                }
            })
        }
    }

    fun startScanning(){
        configViewModel.scanForBleSensor(this@DeviceConfigActivity).observe(
                this@DeviceConfigActivity,
                LceDispatcher(this@DeviceConfigActivity)
        )
        binding.configurationPager.isUserInputEnabled = false
    }

    private fun setupEventListener(){
        configViewModel.eventsObservable.observe(this){
            when(it){
                DeviceConfigurationViewModel.EVENTS.SHOW_OPEN_CHAT_DIALOG -> showChatDialog()
                null -> Timber.d("Event is null")
            }
        }
    }

    fun showChatDialog(){
        AlertDialog.Builder(this)
            .setTitle("Chat in corso")
            .setMessage("Hai una chat in corso con un operatore, vuoi aprire la chat esistente " +
                "o crearne una nuova? Creandone una nuova la chat in corso verrà chiusa")
            .setNeutralButton("Annulla", null)
            .setPositiveButton("Apri chat esistente"){_, _->
                configViewModel.openExistentChat()
            }
            .setNegativeButton("Apri nuova chat"){_,_ ->
                configViewModel.closeExistentAndCreateNewChat()
            }
            .show()
    }

    private fun onConfigButtonClicked(){
        if(binding.configButton.tag == errorButtonTag){
            contactAssistance()
        }else{
            goToNextPage()
        }
    }

    private fun contactAssistance() {
        configViewModel.contactAssistance(this)
    }

    private fun goToNextPage(){
        with(binding){
            val currentPage = configurationPager.currentItem
            val hasNext = currentPage != adapter.itemCount -1
            if(hasNext && currentPage != 2){
                configurationPager.currentItem = currentPage+1
            } else{
                finish()
            }
        }
    }

    //DeviceConfigCallback called when user select wheelDimen
    override fun onWheelDimenConfirmed(valueSelected: String) {
        Timber.d("value $valueSelected")
        configViewModel.setWheelDimen(valueSelected)
    }

    override fun onBikeTypeConfirmed(valueSelected: String) {
        configViewModel.setBikeTypeValue(valueSelected)
    }

    // LCE on sensor
    override fun onLoading() {
        // Scan will automatically exit with LCE error if device is not found for 20 seconds
        with(binding){
            configLoading.visibility = View.VISIBLE
            configButton.visibility = View.INVISIBLE
        }
    }

    override fun onSuccess(data: LisMoveDevice) {
        val currentPage = binding.configurationPager.currentItem
        with(binding){
            configurationPager.currentItem = currentPage+1
            configLoading.visibility = View.INVISIBLE
            configButton.visibility = View.VISIBLE

        }

        // Write in global settings that first local pairing succeded
        configViewModel.setLocalPairingDoneAsync()
    }

    override fun onError(throwable: Throwable) {
        val currentPage = binding.configurationPager.currentItem
        with(binding){
            adapter.error = throwable.message + " Il sensore è acceso e funzionante?"
            configViewModel.viewModelScope.launch(Dispatchers.Main) {
                adapter.notifyItemChanged(3)
                delay(1000)
                configurationPager.currentItem = currentPage+1
                configLoading.visibility = View.INVISIBLE
                configButton.visibility = View.VISIBLE
                configButton.text = "Contatta l'assistenza"
                configButton.tag = errorButtonTag
                Timber.d("create ${configurationPager.currentItem}")
            }





        }

    }

    // Permission handling
    @AfterPermissionGranted(PERMISSIONS_REQUEST_CODE)
    private fun checkPermissions() {
        val genericPermissions = LisMovePermissionsUtils.getGenericPermissions()

        if (!EasyPermissions.hasPermissions(this, *genericPermissions)) {
            EasyPermissions.requestPermissions(
                host = this,
                rationale = getString(R.string.permission_request_rationale),
                requestCode = PERMISSIONS_REQUEST_CODE,
                perms = genericPermissions
            )

            return
        }

        if(LisMovePermissionsUtils.hasToAskBackgroundLocationPermission()) {
            val backgroundPermission =  LisMovePermissionsUtils.getBackgroundLocationPermission()

            if (!EasyPermissions.hasPermissions(this, backgroundPermission)) {
                Timber.i("permissions Denied: background localization")
                EasyPermissions.requestPermissions(
                    host = this,
                    rationale = getString(R.string.permission_background_request_rationale),
                    requestCode = PERMISSIONS_REQUEST_CODE,
                    perms = arrayOf(backgroundPermission)
                )

                Timber.i("Background permission Denied")
                return
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (perms.size == 1 && perms[0] == LisMovePermissionsUtils.getBackgroundLocationPermission()) {
            // App can still work, but will not in background
            Toast.makeText(this, getString(R.string.permission_background_denied_rationale), Toast.LENGTH_SHORT).show()
            return
        }
        // Can't proceed
        Toast.makeText(this, getString(R.string.permission_denied_rationale), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
