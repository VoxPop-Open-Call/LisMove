package it.lismove.app.android.session.ui

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bugsnag.android.Bugsnag
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import it.lismove.app.android.MenuItem
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentSessionManagerBinding
import it.lismove.app.android.deviceConfiguration.DeviceConfigActivity
import it.lismove.app.android.gaming.ui.AchievementActivity
import it.lismove.app.android.general.IS_FORMIGGINI_GYRO_ONLY
import it.lismove.app.android.general.LisMoveApplication
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.maps.MapsActivity
import it.lismove.app.android.session.data.SessionDashBoardData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.lismove.app.android.general.utils.PowerUtils
import it.lismove.app.android.initiative.ui.view.InitiativeRegulationAlertUtils
import it.lismove.app.android.initiative.ui.view.ListAlertDialog
import it.lismove.app.android.maps.AddFountainFragment
import it.lismove.app.android.session.ui.data.*
import it.lismove.app.common.LisMovePermissionsUtils
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber

private const val PERMISSIONS_REQUEST_CODE = 1
class SessionFragment : LisMoveFragment(R.layout.fragment_session_manager) {

    private lateinit var binding: FragmentSessionManagerBinding

    private val sessionViewModel: SessionViewModel by inject()

    private var soundPool: SoundPool? = null
    private val soundId = 1
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    var tempIsStarted = false
    private val textBlinkAnimator: Animator? by lazy {
        context?.let {
            AnimatorInflater.loadAnimator(it, R.animator.text_blink)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        requireActivity().title = "Sessione"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated with theme id${sessionViewModel.theme}")
        binding = FragmentSessionManagerBinding.bind(view)

        binding.sessionDashboardCardView.visibility = View.VISIBLE

        binding.clacsonButton.setOnClickListener { playClacson() }
        binding.reportLayout.setOnClickListener { openReportDialog() }
        binding.mapLayout.setOnClickListener { startActivity(
                MapsActivity.getIntent(requireActivity(), sessionViewModel.activeSession?.id)

        ) }
        binding.lightButton.setOnClickListener {
            sessionViewModel.isFlashOn = !sessionViewModel.isFlashOn
            switchFlashLight(sessionViewModel.isFlashOn)
        }

        binding.initiativePointLayout.setOnClickListener { showPointsDetail()  }
        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            .build()

        soundPool!!.load(requireContext(), R.raw.bycycle_bell_ring, 1)
        sessionViewModel.updateHasAchievement()
        sessionViewModel.initGpsBluetoothAndSensorListeners(requireContext())
        sessionViewModel.initConnectedSensor()
        binding.multiplierValue.setOnClickListener { showBonusDetail() }
        binding.multiplierLabel.setOnClickListener { showBonusDetail() }
        textBlinkAnimator?.setTarget(binding.oneLineOnlyGps)
        setupTorch()
        setupTheme()
        setBluetoothStatusListener()
        setGPSStatusListener()
        setSessionStatusListener()
        setupThemeSwitch()
        observeEvents()
        setupAchievementButton()
        PowerUtils.checkIfBatteryOptimized(requireContext())

        val wasAlertOpen = sessionViewModel.isSessionFloatingOpen()
        Timber.d("WasAlertOpen? $wasAlertOpen")

        showDashBoardAlert(wasAlertOpen)
        updateFabAnimation()

        sessionViewModel.showFabEvent.observe(this) {
            binding.fabMenuView.isVisible = it
        }

        binding.fabMenuView.fabItem.onClickListener = {
            val activeSession = sessionViewModel.activeSession

            if (activeSession == null){
                startSession()
            }else{
                toggleDashboardAlert()
            }
        }

        try {
            sessionViewModel.checkIntentExtras(activity?.intent?.extras, requireContext())
        } catch (e: Exception) { Timber.e(e) }
    }

    private fun setupAchievementButton() {
        with(binding){
            achievementLayout.isVisible = sessionViewModel.hasAchievements
            achievementLayout.setOnClickListener { openAchievements() }

        }
    }

    private fun openReportDialog() {
            AddFountainFragment().show(childFragmentManager, AddFountainFragment.TAG)
    }

    private fun setupThemeSwitch(){
        binding.themeSwitch.isChecked = sessionViewModel.theme != AppCompatDelegate.MODE_NIGHT_YES
        binding.themeSwitch.setThumbDrawableRes(getThemeImage())
        binding.themeSwitch.setBackColorRes(getThemeColorBack())

        binding.themeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Theme: change with $isChecked")
            changeTheme()
        }
    }

    private fun getThemeColorBack(): Int {
        return if(sessionViewModel.theme == AppCompatDelegate.MODE_NIGHT_YES){
            R.color.alwaysOnchipBackgroundDark
        }else {
            R.color.light_gray
        }
    }


    private fun updateFab(){
        updateFabOptions()
        updateFabAnimation()
    }

    private fun updateFabOptions() = sessionViewModel.viewModelScope.launch {
        val activeSession = sessionViewModel.activeSession

        if(activeSession != null && isSessionDashboardVisible()){
            val options:ArrayList<MenuItem> = arrayListOf()
            when {
                activeSession.isRunning() -> {
                    Log.e("testtest", "UPDATE FAB: RUNNING")
                    options.add(MenuItem(getString(R.string.session_pause)) { pauseSession() })
                    options.add(MenuItem(getString(R.string.session_stop_and_send)) { stopSessionAfterConfirmation() })
                }
                activeSession.isPaused()  -> {
                    Log.e("testtest", "UPDATE FAB: PAUSED")
                    options.add(MenuItem(getString(R.string.session_resume)) { resumeSession() })
                    options.add(MenuItem(getString(R.string.session_stop_and_send)) { stopSessionAfterConfirmation() })
                }
                activeSession.isStopped()  ->{
                    Log.e("testtest", "UPDATE FAB: STOPPED")
                    options.add(MenuItem(getString(R.string.session_send)) { sessionViewModel.uploadSession(context) })
                }
                else -> { }
            }

            if(isSessionDashboardVisible()){
                options.add(MenuItem(getString(R.string.session_hide_cruscotto)) { toggleDashboardAlert() })
            }else{
                options.add(MenuItem(getString(R.string.session_show_cruscotto)) { toggleDashboardAlert() })
            }
            binding.fabMenuView.updateMenuItems(options)
        } else {
            binding.fabMenuView.updateMenuItems(listOf())
            binding.fabMenuView.closeFabMenu()
        }
    }

    private fun isSessionDashboardVisible(): Boolean {
        return binding.sessionDashboardCardView.isVisible
    }

    private fun toggleDashboardAlert() {
        sessionViewModel.setSessionFloatingOpen(!isSessionDashboardVisible())
        showDashBoardAlert(!isSessionDashboardVisible())
        setAlwaysOn(isSessionDashboardVisible())
    }

    fun showDashBoardAlert(show: Boolean){
        try{
            toggleDashboardView(show)
            binding.fabMenuView.translationZ = 10f
            updateFabOptions()

        } catch (e: Exception){
            Timber.d(e.localizedMessage)
        }
    }

    private fun updateFabAnimation(){
        if (sessionViewModel.activeSession == null){
            Timber.d("SetupFab: no active session")
            binding.fabMenuView.setFabAnimationNormal()
        }else if(sessionViewModel.activeSession?.status == SessionDataEntity.SESSION_STATUS_RUNNING ||
            sessionViewModel.activeSession?.status == SessionDataEntity.SESSION_STATUS_CREATED) {
            Timber.d("SetupFab:  active session")
            binding.fabMenuView.setFabAnimationRotation()
        }else{
            Timber.d("SetupFab:  active paused")
            binding.fabMenuView.setFabAnimationBlink(sessionViewModel.viewModelScope)
        }
    }

    private fun toggleDashboardView(show: Boolean) {
        Timber.d("Toggle $show")
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration = 400
        transition.addTarget(binding.sessionDashboardCardView)
        TransitionManager.beginDelayedTransition(binding.mainLayout, transition)
        binding.sessionDashboardCardView.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setSessionStatusListener() {
        Timber.d("SET SESSION LISTENER")
        sessionViewModel.fetchSessionState().observe(viewLifecycleOwner) {
            Timber.d("sessionChanged ${it}")
            if(it != SessionLoading) hideLoadingAlert()

            sessionViewModel.refreshSensorState()

            when(it){
                is SessionStateInitial -> {
                    tempIsStarted = false
                    updateFab()
                    Log.e("testtest", "INITIAL")
                }
                SessionStateNone -> {
                    updateFab()
                    tempIsStarted = false
                    setupEmptySession()
                    Log.e("testtest", "NONE")
                }
                is SessionStateStarted -> {
                    // in case a new session is started, mark the previous one
                    // as already uploaded
                    sessionViewModel.isUploadingSession = false
                    setupSessionStartedUpdated(it.updatedSessionData)


                    if(!tempIsStarted){
                        tempIsStarted = true
                        Log.e("testtest", "temp set to true")
                        updateFab()
                    } else {
                        // FIX FOR #88
                        // Sometimes we miss the started update and the FAB shows only 1 option ("Hide dashboard");
                        // This checks if this is the case and eventually refreshes the fab (since session here is started)
                        /*if (binding.fabMenuView.getMenuItemsCount() <= 1) {
                            Log.e("testtest", "temp is started FALSE")
                            updateFab()
                        }*/
                    }
                }
                is SessionStatePaused -> {
                    updateFab()
                    setupSessionPaused(it.lastSessionData)
                    tempIsStarted = false
                    Log.e("testtest", "PAUSED")

                    Log.e("pause_restore", "Last session data time: " + it.lastSessionData.time)
                }

                is SessionStateStopped -> {
                    Timber.d("sessionStateStopped ")
                    if (!sessionViewModel.isUploadingSession) {
                        sessionViewModel.isUploadingSession = true
                        sessionViewModel.uploadSession(context)
                    }

                    updateFab()
                    setupSessionStopped(it.lastSessionData)
                    tempIsStarted = false
                    Log.e("testtest", "STOPPED")
                }
                is SessionUploaded -> {
                    updateFab()
                    openSessionDetail(it.sessionId)
                    tempIsStarted = false
                    Log.e("testtest", "UPLOADED")
                }
                SessionLoading -> {
                    toggleDashboardAlert()
                    showLoadingAlert()
                    // updateFab()
                }

                is SessionShowErrorDialog -> showAlertDialog(it.title, it.message)
                is ShowSensorConfigurationPopup -> openGoToConfigurationPopup()
                is SessionUploadError -> showAlertDialog(it.title, it.message){}
            }
        }
    }

    private fun openGoToConfigurationPopup(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Configura")
        builder.setMessage("Lis Move non è stato ancora configurato correttamente, premi il tasto Configura per un corretto funzionamento dell'applicazione")
        builder.setPositiveButton(
            "Configura"
        ) { dialog, id ->
            dialog.dismiss()
            goToBikeConfig()
        }
        builder.setNegativeButton("ANNULLA") { dialog, id -> }
        builder.show()
        sessionViewModel.onPopupSent()
    }

    private fun openSessionDetail(sessionId: String){
        startActivity(SessionDetailActivity.getIntent(requireContext(), sessionId, false))
    }

    private fun setupEmptySession(){
        showDashBoardAlert(false)
        Timber.d("setupEmptySession")
        binding.sessionStateTitle.text = "Nessuna sessione in corso"
        binding.sessionStateTitle.setTextColor(resources.getColor(R.color.gray_image_tint, null))
        setupDashboardData(SessionDashBoardData())
    }

    fun setupSessionStartedUpdated(sessionData: SessionDashBoardData){
        Timber.d("setupSessionStartedUpdated")
        setupDashboardData(sessionData)
        setupAchievementButton()
        binding.sessionStateTitle.text = "Sessione in corso"
        binding.sessionStateTitle.setTextColor(resources.getColor(R.color.green, null))

    }

    fun setupSessionPaused(sessionData: SessionDashBoardData){
        Timber.d("setupSessionPaused")
        binding.sessionStateTitle.text = "Sessione in pausa"
        binding.sessionStateTitle.setTextColor(resources.getColor(R.color.yellowColor, null))
        setupDashboardData(sessionData)
    }

    fun setupSessionStopped(sessionData: SessionDashBoardData){
        Timber.d("setupSessionStopped")
        binding.sessionStateTitle.text = "Sessione terminata"
        binding.sessionStateTitle.setTextColor(resources.getColor(R.color.yellowColor, null))
        setupDashboardData(sessionData)
    }

    private fun setupDashboardData(sessionData: SessionDashBoardData){
        Timber.d("setupDashboardData")
        Log.e("testtest", "setupDashboardData: ${sessionData.time}")
        with(binding) {
            sessionDistance.text = sessionData.distance
            sessionCurrentVelocity.text = sessionData.speed
            sessionTime.text = sessionData.time
            sessionVelocity.text = sessionData.avgSpeed
            dashboardNationalPoints.text = sessionData.nationalPoints
            dashboardInitiativePoints.text = sessionData.initiativePoints
            initiativePointsLabel.text = getString(R.string.session_manager_initiative_points, sessionData.activeInitiatives.toString())
            activeInitiatives.text = sessionData.activeInitiatives.toString()
            batteryImage.visibility = if (sessionData.sensorBatteryIcon != null) View.VISIBLE else View.GONE
            setupUrbanOrInitiativeIcon(sessionData.urban)
            multiplierValue.text =  sessionData.multiplierValue
            multiplierLabel.text = getString(R.string.session_manager_multiplier_number,"${sessionData.multiplierLabelEnd}")
            multiplierValue.isVisible = sessionData.showMultiplier
            multiplierLabel.isVisible = sessionData.showMultiplier
            setupAchievementButton()
            sessionData.sensorBatteryIcon?.let {
                batteryImage.setImageResource(it)
            }

            bonusLayout.setOnClickListener { showRegulationDialog()  }
        }

        if (sessionData.isGps) {
            showGpsOnlyAlert()
        } else {
            hideGpsOnlyAlert()
        }

        if (!sessionData.isSensorBatteryAvailable) {
            binding.batteryImage.visibility = View.GONE
        }
    }

    private fun openAchievements() {
        context?.let {
            startActivity(Intent(it, AchievementActivity::class.java))
        }
    }

    private fun showRegulationDialog() {
        context?.let {
            InitiativeRegulationAlertUtils.getAlertDialogList(it, sessionViewModel.regulationList).show()
        }
    }

    fun setupUrbanOrInitiativeIcon(isUrban: Boolean){
        context?.let {

            val redColor = it.getColor(R.color.red_main)
            val nationalImage = AppCompatResources.getDrawable(it, R.drawable.ic_extra_urban)
            val initiativeImage = AppCompatResources.getDrawable(it, R.drawable.ic_urban)?.apply {
                setTint(redColor)
            }

            val image = if(isUrban) initiativeImage else nationalImage

            with(binding.nationalOrInitiativeLogo){
                setImageDrawable(image)
            }
        }
    }

    private fun goToBikeConfig(){
        startActivity(Intent(requireActivity(), DeviceConfigActivity::class.java))
    }

    private fun pauseSession(){
        sessionViewModel.pauseSession()
        showManualPauseAlertIfEnabled()
    }

    private fun resumeSession(){
        checkPermissions {
            if (sessionViewModel.gpsConnectivity.value == true) {
                sessionViewModel.resumeSession()
            } else {
                showGpsNotEnabledError()
            }
        }
    }

    private fun stopSessionAfterConfirmation(){
        context?.let { ctx ->
            val dialog = AlertDialog.Builder(ctx)
                .setTitle("Terminare e inviare la sessione?")
                .setPositiveButton("Ok"){_,_ -> sessionViewModel.stopSession(ctx)}
                .setNeutralButton("Elimina\nSessione"){_,_ -> sessionViewModel.clearSession(ctx)}
                .setNegativeButton("Annulla", null)
                .show()

            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).isSingleLine = false
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).isSingleLine = false
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isSingleLine = false

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).minLines = 2
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).minLines = 2

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).layoutParams =  LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).layoutParams =  LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }

    }

    private fun startSession() {
        Timber.d("startSession")
        checkPermissions {
            sessionViewModel.viewModelScope.launch {
                if (sessionViewModel.isLocalPairingDone()) {
                    if (sessionViewModel.isSensorConnected()) {
                        Timber.d("permissionGranted")
                        sessionViewModel.startSession(requireContext())
                    } else {
                        if (LisMoveApplication.isFormiggini()) {
                            if (IS_FORMIGGINI_GYRO_ONLY) {
                                errorSensorNotConnected()
                            } else {
                                warnSensorNotConnectedIfEnabled()
                            }
                        } else {
                            warnSensorNotConnectedIfEnabled()
                        }
                    }
                } else {
                    askForLocalSensorPairing()
                }
            }
        }
    }

    private fun askForLocalSensorPairing() {
        with(AlertDialog.Builder(requireContext())) {
            setTitle("Associa un sensore")
            setMessage("Lis Move non è stato ancora configurato correttamente su questo dispositivo.\nAccendi il tuo sensore LisMove e premi il tasto configura.\n\nIn alternativa, avvia una sessione in modalità solo GPS.")
            setPositiveButton("Configura sensore") { dialog, id ->
                dialog.dismiss()
                goToBikeConfig()
            }

            if (LisMoveApplication.isFormiggini()) {
                if (!IS_FORMIGGINI_GYRO_ONLY) {
                    setNegativeButton("Avvia in modalità GPS") { dialog, id ->
                        sessionViewModel.startSession(requireContext())
                        dialog.dismiss()
                    }
                } else {}
            } else {
                setNegativeButton("Avvia in modalità GPS") { dialog, id ->
                    sessionViewModel.startSession(requireContext())
                    dialog.dismiss()
                }
            }

            show()
        }
    }

    private fun warnSensorNotConnectedIfEnabled(){
        if (sessionViewModel.WARN_SENSOR_NOT_CONNECTED_BEFORE_SESSION) {
            with(AlertDialog.Builder(requireContext())) {
                setTitle("Sessione GPS")
                setMessage(getSensorNotConnectedReason())
                setPositiveButton("Avvia sessione GPS") { dialog, id ->
                    sessionViewModel.startSession(requireContext())
                    dialog.dismiss()
                }
                setNegativeButton("Annulla") { dialog, id ->
                    dialog.dismiss()
                }

                show()
            }
        } else {
            sessionViewModel.startSession(requireContext())
        }
    }

    private fun getSensorNotConnectedReason(): String{
        return if(sessionViewModel.bluetoothConnectivity.value != true){
             "Stai per effettuare una sessione in modalità solo GPS, poichè il bluetooth è disattivato.\n" +
                    "Se procedi, i dati sulla sessione potrebbero essere imprecisi."
        }else{
            "Stai per effettuare una sessione in modalità solo GPS, poichè LisMove non è connesso al telefono.\n" +
                    "Se procedi, i dati sulla sessione potrebbero essere imprecisi."
        }
    }
    private fun errorSensorNotConnected(){
        val message = if(sessionViewModel.bluetoothConnectivity.value != true){
            "Il bluetooth è disattivato, riattivalo per collegare il sensore e riprova"
        }else{
            "Assicurati che il tuo sensore sia acceso e connesso a Lis Move (premi il pulsante nella scheda \"Dispositivi Associati\" per riprovare. Se il problema persiste, riassocia il tuo sensore)."
        }
        with (AlertDialog.Builder(requireContext())) {
            setTitle("Sensore non connesso")
            setMessage(message)
            setPositiveButton("OK") { dialog, id ->
                dialog.dismiss()
            }

            setNegativeButton("Associa sensore") { dialog, id ->
                activity?.let {
                    sessionViewModel.reconfigureSensor(it)
                }
                dialog.dismiss()
            }

            show()
        }
    }

    private fun observeEvents() {
        sessionViewModel.associatedSensorObservable.observe(viewLifecycleOwner) {
            with(binding.sensorChip) {

                if (it?.connected == true) {
                    text = it.sensor.name
                    setOnClickListener { }
                } else {
                    text = getString(R.string.sensor_not_connected)
                    setOnClickListener { showSensorNotConnectedDialog() }
                }
            }
        }

        sessionViewModel.sessionEventObservable.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        sessionViewModel.initConnectedSensor()
    }

    private fun setGPSStatusListener(){
        sessionViewModel.gpsConnectivity.observe(viewLifecycleOwner, {
            val color = if (it) R.color.green else R.color.gray_image_tint
            binding.gpsIndicator.imageTintList = AppCompatResources.getColorStateList(requireContext(), color)

            if (!it) {
                showGpsNotEnabledError()
            }
        })
    }

    private fun showManualPauseAlertIfEnabled() {
        if(sessionViewModel.manualPauseAlertEnabled){
            context?.let {
            AlertDialog.Builder(it)
                .setMessage("Hai messo in pausa manualmente la sessione, per riavviarla dovrai procedere manualmente. Ti ricordiamo che il sistema può andare in pausa e riavviare la sessione in maniera automatica")
                .setPositiveButton("OK", null)
                .setNeutralButton("Non visualizzare più"){_, _ -> sessionViewModel.setPauseAlertDoNotShowAgain()}
                .show()
            }
        }

    }

    private fun showGpsNotEnabledError() {
        val title = "Servizio di localizzazione disabilitato"
        val message = "Attiva il servizio di localizzazione GPS per riprendere la sessione"
        showAlertDialog(title, message)
    }

    // Temp variable for bluetooth cache.
    // If bluetooth is disabled and enabled within 5 seconds, no errors will be displayed
    // Display an error after 6 seconds of bluetooth deactivation
    private var tempBluetoothEnabled = true
    private fun setBluetoothStatusListener(){
        sessionViewModel.bluetoothConnectivity.observe(viewLifecycleOwner, {isBluetoothAvailable ->
            tempBluetoothEnabled = isBluetoothAvailable

            val drawable =
                if (isBluetoothAvailable) R.drawable.ic_baseline_bluetooth_audio_24 else R.drawable.ic_baseline_bluetooth_disabled_24

            binding.bluetoothIndicator.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    drawable
                )
            )

            if (isBluetoothAvailable.not()) {
                lifecycleScope.launchWhenCreated {
                    // Wait 6 seconds before displaying error. If bluetooth is returned in
                    // that time, do not display anything
                    delay(6000)
                    if (!tempBluetoothEnabled) {
                        val title = "Bluetooth disabilitato o non disponibile"
                        val message =
                            "Lis Move utilizza dispositivi BLE per tracciare le tue corse in bici"
                        showAlertDialog(title, message)
                    }
                }
            }
        })
    }


    @AfterPermissionGranted(PERMISSIONS_REQUEST_CODE)
    private fun checkPermissions(onPermissionGranted: ()->Unit) {
        val genericPermissions = LisMovePermissionsUtils.getGenericPermissions()

        if (!EasyPermissions.hasPermissions(requireContext(), *genericPermissions)) {
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

            if (!EasyPermissions.hasPermissions(requireContext(), backgroundPermission)) {
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

        onPermissionGranted()
    }

    private fun playClacson(){
        try {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            binding.clacsonButton.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.yellowColor, null))
            GlobalScope.launch {
                delay(2000)
                activity?.runOnUiThread {
                    binding.clacsonButton.imageTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.gray_image_tint, null))
                }
            }
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
        }
    }

    private fun setupTorchButton(){
        Timber.d("Setup toarch, isOn: ${sessionViewModel.isFlashOn}")
        if(sessionViewModel.isFlashOn){
            binding.lightButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.yellowColor, null))

        }else{
            binding.lightButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.gray_image_tint, null))
        }
    }
    private fun showNoFlashError() {
        val alert = AlertDialog.Builder(requireContext())
            .create()
        alert.setTitle("Flash non disponibile")
        alert.setMessage("Impossibile accedere al flash")
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { dialog, _ ->  dialog.dismiss()}
        alert.show()
    }
    private fun switchFlashLight(status: Boolean) {
        setupTorchButton()
        try {
            cameraManager.setTorchMode(cameraId, status)

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setupTorch(){
        setupTorchButton()
        val isFlashAvailable = requireContext().packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!isFlashAvailable) {
            showNoFlashError()
        }
        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    fun showGpsOnlyAlert(){
        with(binding){
            batteryImage.visibility = View.GONE

            oneLineOnlyGps.visibility = View.VISIBLE
            oneLineOnlyGps.isVisible = true

            textBlinkAnimator?.start()
        }
    }

    fun hideGpsOnlyAlert(){
        with(binding){
            batteryImage.visibility = View.VISIBLE
            oneLineOnlyGps.visibility = View.INVISIBLE
            textBlinkAnimator?.cancel()
        }
    }



    fun changeTheme(){
        sessionViewModel.changeTheme()
        Timber.d("Change theme ${sessionViewModel.theme}")
        setupTheme()
    }

    fun setupTheme() {

        var backgroundColor =
            if (sessionViewModel.theme == 2) requireContext().getColor(R.color.alertBackgroundDark) else requireContext().getColor(
                R.color.background_light
            )
        var textTitle =
            if (sessionViewModel.theme == 2) requireContext().getColor(R.color.textDark) else requireContext().getColor(
                R.color.text_primary_light
            )
        var divider = requireContext().getColor(R.color.light_gray)

        var chipBackground =
            if (sessionViewModel.theme == 2) R.color.chipBackgroundDark else R.color.chipBackgroundLight

        var switchTintColor =  if (sessionViewModel.theme == 2) R.color.white else R.color.red_main
        with(binding) {
            cardConstraintLayout.setBackgroundColor(backgroundColor)
            titleDivider.root.setBackgroundColor(divider)
            timeDivider.root.setBackgroundColor(divider)
            velocityDivider.root.setBackgroundColor(divider)
            verticalDivider.setBackgroundColor(divider)
            pointsDivider.setBackgroundColor(divider)
            sessionTime.setTextColor(textTitle)
            sessionTimeLabel.setTextColor(textTitle)
            sessionVelocity.setTextColor(textTitle)
            sessionVelocityLabel.setTextColor(textTitle)
            sessionVelocityUnit.setTextColor(textTitle)
            sessionCurrentVelocity.setTextColor(textTitle)
            sessionCurrentVelocityLabel.setTextColor(textTitle)
            sessionCurrentVelocityUnit.setTextColor(textTitle)
            sessionDistance.setTextColor(textTitle)
            sessionDistanceUnit.setTextColor(textTitle)
            sessionDistanceLabel.setTextColor(textTitle)
            textView5.setTextColor(textTitle)
            activeInitiativesLabel.setTextColor(textTitle)
            activeInitiatives.setTextColor(textTitle)
            pointsLabel.setTextColor(textTitle)
            textView19.setTextColor(textTitle)
            sensorChip.setChipBackgroundColorResource(chipBackground)
            sensorChip.setTextColor(textTitle)
            sensorChip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_outline_bluetooth_24)
            themeSwitch.setThumbDrawableRes(getThemeImage())
            themeSwitch.setBackColorRes(getThemeColorBack())

        }
    }

    fun getThemeImage(): Int{
        return if(sessionViewModel.theme == AppCompatDelegate.MODE_NIGHT_YES){
            R.drawable.ic_moon
        }else {
            R.drawable.ic_sun3
        }
    }

    private fun showSensorNotConnectedDialog() {
        with (AlertDialog.Builder(requireContext())) {
            setTitle("Sensore non connesso")
            setMessage("Nessun sensore connesso, vuoi terminare la sessione e associarne uno?")
            setPositiveButton("Termina sessione") { dialog, id ->
                sessionViewModel.stopSession(requireContext())
                dialog.dismiss()
            }
            setNegativeButton("Continua") { dialog, id ->
                dialog.dismiss()
            }

            show()
        }
    }

    private fun showPointsDetail(){
        val dialog = ListAlertDialog.build(requireContext(), "Punti progetto") {item, dialog ->}
        dialog.show()
        sessionViewModel.getPoints().observe(requireActivity()){
            when(it){
                is LceLoading -> {dialog.showLoading()}
                is LceSuccess ->{dialog.setData(it.data)}
                is LceError -> {showError(it.error.localizedMessage ?: "")}
            }
        }
    }

    fun showBonusDetail(){
        Timber.d("ShowBounsDetail")
        val dialog = ListAlertDialog.build(requireContext(), "Moltiplicatori attivi") {item, dialog ->}
        dialog.show()
        sessionViewModel.getBonusList().observe(requireActivity()){
            when(it){
                is LceLoading -> {dialog.showLoading()}
                is LceSuccess ->{dialog.setData(it.data)}
                is LceError -> {showError(it.error.localizedMessage ?: "")}
            }
        }
    }

    private fun setAlwaysOn(enabled: Boolean){
        if (enabled) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}