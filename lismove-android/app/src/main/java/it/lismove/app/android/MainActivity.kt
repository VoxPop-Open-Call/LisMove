package it.lismove.app.android

import android.Manifest
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.snackbar.Snackbar
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.zoho.salesiqembed.ZohoSalesIQ
import it.lismove.app.android.awards.AwardWrapperFragment
import it.lismove.app.android.dashboard.DashboardFragment
import it.lismove.app.android.databinding.ActivityMainBinding
import it.lismove.app.android.deviceConfiguration.DeviceConfigActivity
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.maps.MapsFragment
import it.lismove.app.android.other.OtherFragment
import it.lismove.app.android.gaming.ui.AchievementFragment
import it.lismove.app.android.gaming.ui.ActiveAwardsFragment
import it.lismove.app.android.gaming.ui.RankingFragment
import it.lismove.app.android.general.BUILD_VARIANT_DEV
import it.lismove.app.android.logWall.LogWallFragment
import it.lismove.app.android.notification.ui.NotificationListActivity
import it.lismove.app.android.other.UriUtils
import it.lismove.app.android.session.ui.SessionFragment
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.sensorUpgrade.DeviceUpgradeActivity
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber

//TODO: Fragment lazy initialization

private const val PERMISSIONS_REQUEST_CODE = 1
class MainActivity : LisMoveBaseActivity() {
    lateinit var binding: ActivityMainBinding
    val viewModel: MainActivityViewModel by inject()
    var sessionDashBoardFragment: SessionFragment? = null
    var requestFragmentUpdate = false
    lateinit var rankingFragment: RankingFragment
    lateinit var activeAwardsFragment: ActiveAwardsFragment
    lateinit var awardFragment: AwardWrapperFragment
    lateinit var achievementFragment: AchievementFragment
    lateinit var logWallFragment: LogWallFragment

    @RequiresApi(Build.VERSION_CODES.S)
    private var PERMISSIONS_BLE_S: Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZohoSalesIQ.showLauncher(false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        val dashboardFragment = DashboardFragment()
        val otherFragment = OtherFragment()
        val mapsFragment = MapsFragment()
        logWallFragment = LogWallFragment()

        awardFragment = AwardWrapperFragment()
        achievementFragment = AchievementFragment()
        rankingFragment = RankingFragment()
        activeAwardsFragment = ActiveAwardsFragment()
        sessionDashBoardFragment = SessionFragment()
        supportFragmentManager.beginTransaction().replace(R.id.sessionFragmentView,
            sessionDashBoardFragment!!).commit()

        viewModel.getNotificationIdIfNotificationClicked(intent)?.let {
            startActivity(NotificationListActivity.getIntent(this, it.toLong()))
        }
        viewModel.sendNotUploadedSessions()
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            Timber.d("Navigation item selected")
            closeSessionPanel()
            when (it.itemId) {
                R.id.nav_menu_home -> {
                    setCurrentFragment(dashboardFragment)
                }
                R.id.nav_menu_map -> {
                    setCurrentFragment(mapsFragment)
                }
                R.id.nav_menu_gaming -> {
                    if(requestFragmentUpdate){
                        requestFragmentUpdate = false
                        return@setOnNavigationItemSelectedListener true
                    }else{
                        requestGamingOption()
                        return@setOnNavigationItemSelectedListener false
                    }
                }
                R.id.nav_menu_log_wall -> {
                    setCurrentFragment(logWallFragment)
                }

                R.id.nav_menu_other -> {
                    setCurrentFragment(otherFragment)
                }
                else -> {
                    setCurrentFragment(dashboardFragment)
                }
            }
            true
        }

        binding.bottomNavigationView.selectedItemId = R.id.nav_menu_home
        checkSensorConfiguredOrShowAlert()

        viewModel.messageObservable.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.sensorNotFoundObservable.observe(this) {
            val deviceNotFoundSnackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.sensor_not_detected),
                Snackbar.LENGTH_LONG)

            deviceNotFoundSnackbar.show()
        }

        viewModel.sensorUpdateObservable.observe(this) { macAddress ->
            lifecycleScope.launch {

                val dialogMessage = if (viewModel.willForceSensorUpdate()) {
                    "Per usufluire degli ultimi miglioramenti, è necessario aggiornare il sensore Lismove ora."
                } else {
                    "E' disponibile un nuovo aggiornamento per il sensore Lis Move. Aggiornare ora?"
                }

                with(AlertDialog.Builder(this@MainActivity)) {
                    setTitle("Nuovo aggiornamento disponibile")
                    setMessage(dialogMessage)
                    setCancelable(false)
                    setPositiveButton("Si") { dialog, _ ->
                        viewModel.startSensorUpgrade(macAddress, this@MainActivity)
                        dialog.dismiss()
                    }

                    if (!viewModel.willForceSensorUpdate()) {
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

        viewModel.sensorInDfuObservable().observe(this) { found ->
            if (!viewModel.hasAlreadyShownDfuSensor) {
                if (found != null) {
                    with(AlertDialog.Builder(this@MainActivity)) {
                        setTitle("Aggiornamento sensore richiesto")
                        setMessage("E' stato rilevato un sensore che non ha completato correttamente l'aggiornamento del firmware.\n\nE' necessario eseguire l'aggiornamento ora per poterlo usare.")
                        setCancelable(false)
                        setPositiveButton("Aggiorna") { dialog, _ ->
                            dialog.dismiss()
                            viewModel.startSensorUpgradeWithoutMac(this@MainActivity)
                        }
                        setNegativeButton("Ignora") { dialog, _ ->
                            dialog.dismiss()
                        }

                        show()
                        viewModel.hasAlreadyShownDfuSensor = true
                    }
                }
            }
        }

        checkPermissions {
            viewModel.ensureConnectedToSensor(this)
            viewModel.startBackgroundDetection(this)
            viewModel.observeBleStatus()
            viewModel.observeOfflineHistory()
            checkIfIsLatestVersion()
            checkLowBattery()
        }
    }

    private fun requestGamingOption() {
        var bottomNavigationBar = binding.bottomNavigationView[0] as BottomNavigationMenuView
        var rankingItem = bottomNavigationBar[2]
        openRankingMenu(rankingItem)
    }

    private fun closeSessionPanel(){
        Timber.d("CloseSessionPanel")
        sessionDashBoardFragment?.showDashBoardAlert(false)
    }

    fun openRankingMenu(view: View) {

        val popupMenu = popupMenu {
            style = R.style.Widget_MPM_Menu_Dark_CustomBackground
            section {
                item {
                    label = "Classifiche"
                    icon = R.drawable.ic_leaderboard
                    callback = { openRanking() }
                }
                item {
                    label = "Coppe"
                    icon = R.drawable.ic_emoji_events
                    callback = { openAchievement() }
                }/*
                item{
                    label = "Premi e incentivi"
                    icon = R.drawable.ic_notification_session
                    callback = {openActiveAwards()}
                }*/
               item {
                    label = "I miei premi"
                    icon = R.drawable.ic_notification_session
                    callback = { openAwards() }
                }
            }

        }

        popupMenu.show(this@MainActivity, view)
    }

    private fun openActiveAwards() {
        setCurrentFragment(activeAwardsFragment)
        setGamingAsSelected()
    }

    private fun openAwards(){
        setCurrentFragment(awardFragment)
        setGamingAsSelected()
    }

    private fun openAchievement(){
        setCurrentFragment(achievementFragment)
        setGamingAsSelected()
    }

    private fun openRanking(){
        setCurrentFragment(rankingFragment)
        setGamingAsSelected()
    }

    private fun setGamingAsSelected(){
        requestFragmentUpdate = true
        binding.bottomNavigationView.selectedItemId = R.id.nav_menu_gaming
    }

    private fun checkSensorConfiguredOrShowAlert(){
        viewModel.viewModelScope.launch {
            if(viewModel.showSensorConfigAlert()){
                val intent = Intent(this@MainActivity, DeviceConfigActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkIfIsLatestVersion() {
        if (BuildConfig.FLAVOR != BUILD_VARIANT_DEV) {
            viewModel.warnAboutVersionEvent.observe(this) {
                lifecycleScope.launchWhenStarted {
                    if (!viewModel.hasActiveSessionRunning()) {
                        with(AlertDialog.Builder(this@MainActivity)) {
                            setTitle("Nuovo aggiornamento disponibile")
                            setMessage("È disponibile una nuova versione dell'app.")
                            setPositiveButton("Aggiorna") { dialog, _ ->
                                UriUtils.openUri(
                                    "https://appdistribution.firebase.google.com",
                                    this@MainActivity
                                )
                            }
                            setNegativeButton("Più tardi") { dialog, _ ->
                                dialog.dismiss()
                            }

                            show()
                        }
                    }
                }
            }

            viewModel.requireNewVersionEvent.observe(this) {
                lifecycleScope.launchWhenStarted {
                    if (!viewModel.hasActiveSessionRunning()) {
                        with(AlertDialog.Builder(this@MainActivity)) {
                            setTitle("Aggiornamento richiesto")
                            setMessage("Per usufluire di nuove funzionalità, è richiesta una nuova versione dell'app.")
                            setPositiveButton("Aggiorna ora") { dialog, _ ->
                                UriUtils.openUri(
                                    "https://appdistribution.firebase.google.com",
                                    this@MainActivity
                                )

                                finish()
                            }

                            setCancelable(false)
                            show()
                        }
                    }
                }
            }
        }
    }

    private fun checkLowBattery() {
        viewModel.sensorLowBatteryEvent.observe(this) {
            with(AlertDialog.Builder(this)) {
                setTitle("Batteria sensore scarica")
                setMessage("La batteria del sensore LisMove è in esaurimento, si consiglia di sostituirla.")
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }

                setCancelable(true)
                show()
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_CODE)
    private fun checkPermissions(onPermissionGranted: ()->Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!EasyPermissions.hasPermissions(this, *PERMISSIONS_BLE_S)) {
                EasyPermissions.requestPermissions(
                    host = this,
                    rationale = getString(R.string.permission_background_request_rationale),
                    requestCode = PERMISSIONS_REQUEST_CODE,
                    perms = PERMISSIONS_BLE_S
                )
            }
        }

        onPermissionGranted()
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.ensureConnectedToSensor(this)
        } catch (e: Exception){}
    }
}

