package it.lismove.app.android.session.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.background_detect.SensorDetectionManager
import it.lismove.app.android.deviceConfiguration.DeviceConfigActivity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.android.gaming.repository.AchievementRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.general.utils.toSingleEvent
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.initiative.ui.data.RegulationListItem
import it.lismove.app.android.initiative.ui.parser.asRegulationListItem
import it.lismove.app.android.prefs.AlertPreferencesRepository
import it.lismove.app.android.session.data.SessionDashBoardData
import it.lismove.app.android.session.data.SessionEvent
import it.lismove.app.android.session.parser.asSessionDashBoardData
import it.lismove.app.android.session.ui.data.*
import it.lismove.app.android.session.useCases.SessionUploadUseCase
import it.lismove.app.android.session.useCases.impl.SessionNotValidException
import it.lismove.app.android.theme.ThemeRepository
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SensorEntity
import it.lismove.app.room.entity.SessionDataEntity
import it.lismove.app.utils.TempPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.SplashScreenExtras
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.models.LisMoveServiceStatus
import net.nextome.lismove_sdk.statusListener.BluetoothStatusListener
import net.nextome.lismove_sdk.statusListener.GpsStatusListener
import net.nextome.lismove_sdk.utils.BugsnagUtils
import timber.log.Timber
import java.util.*

class SessionViewModel(
    context: Context,
    private val sensorSdk: LismoveSensorSdk,
    private val sensorRepository: SensorRepository,
    private val sessionSdkRepository: SessionSdkRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val themeRepository: ThemeRepository,
    private val sessionUploadUseCase: SessionUploadUseCase,
    private val alertPreferencesRepository: AlertPreferencesRepository,
    private val tempPrefsRepository: TempPrefsRepository,
    private val lismoveUser: LisMoveUser,
): ViewModel() {

    val WARN_SENSOR_NOT_CONNECTED_BEFORE_SESSION = false

    var activeInitiatives: List<EnrollmentWithOrganizationAndSettings> = listOf()
    // Map associated sensor with it's connected state
    private val _associatedSensorFlow = MutableStateFlow<SensorWithConnectionState?>(null)
    val associatedSensorObservable = _associatedSensorFlow.asLiveData()

    private val _sessionEvents = MutableStateFlow<SessionEvent?>(null)
    val sessionEventObservable = _sessionEvents.asLiveData()

    lateinit var bluetoothConnectivity: LiveData<Boolean>
    lateinit var gpsConnectivity: LiveData<Boolean>

    var activeSession: SessionDataEntity? = null
    var isFirstStart = true

    private val _sessionStateFlow = MutableStateFlow<SessionState>(SessionStateInitial)

    val sessionState = MediatorLiveData<SessionState>()
    var lastDashBoardData: SessionDashBoardData? = null
    var isFlashOn = false

    private var associatedSensor: SensorEntity? = null

    var isUploadingSession: Boolean = false

    var theme: Int = themeRepository.getTheme()

    var regulationList: List<RegulationListItem> = listOf()
        get() = activeInitiatives.map { it.organization.asRegulationListItem() }

    val manualPauseAlertEnabled: Boolean
        get() = alertPreferencesRepository.showManualPauseAlert()

    val showFabEvent = MutableLiveData<Boolean>()
    var hasAchievements: Boolean = false

    init {
        sessionState.addSource(_sessionStateFlow.asLiveData()) { sessionState.value = it }
        sessionState.addSource(getSensorDataObservable(context)) {
            if (activeSession?.isCreated() == true || activeSession?.isRunning() == true) {
                it?.let {
                    lastDashBoardData = it.asSessionDashBoardData(activeInitiatives)
                    sessionState.value = SessionStateStarted(it.asSessionDashBoardData(activeInitiatives))
                }
            }
        }

        sessionState.addSource(sensorSdk.observeServiceStatus(context)) { it?.let {
            when(it.status){
                LisMoveServiceStatus.FATAL_ERROR -> {
                    sendEvent(it.errorMessage)
                    stopSession(context)
                    reportIssueOnBugsnag(Exception(it.errorMessage))
                }
                LisMoveServiceStatus.WORKING -> {}
                LisMoveServiceStatus.GENERIC_ERROR -> {
                    Timber.e(it.errorMessage)
                }
            } }

            Timber.d(it?.errorMessage)
        }
    }

    fun initGpsBluetoothAndSensorListeners(context: Context){
        bluetoothConnectivity = BluetoothStatusListener(context).toSingleEvent()
        gpsConnectivity = GpsStatusListener(context).toSingleEvent()
    }

    private fun reportIssueOnBugsnag(exception: Exception) {
        BugsnagUtils.reportIssue(exception)
    }

    fun fetchSessionState(): LiveData<SessionState>{
        updateSessionInfo()
        return sessionState
    }

    private fun updateSessionInfo(){
        viewModelScope.launch {
            activeInitiatives = userRepository.getActiveInitiativesWithSettings(lismoveUser.uid)
            sessionSdkRepository.getActiveSessionObservable().collect {
                if (isFirstStart) {
                    isFirstStart = false
                    showFabEvent.postValue(true)
                }

                val prevStatus = activeSession?.status
                activeSession = it
                if(prevStatus != activeSession?.status ){
                    Timber.d("Session $prevStatus - ${activeSession?.status}")
                    updateSessionStateFlow(it)
                }
            }
        }
    }

    private suspend fun updateSessionStateFlow(activeSession: SessionDataEntity?){
        if(activeSession == null){
            _sessionStateFlow.emit(SessionStateNone)
        }else{
            when {
                activeSession.isCreated() -> onSessionCreated()
                activeSession.isRunning() -> onSessionRunning()
                activeSession.isStopped() -> onSessionStopped()
                activeSession.isPaused() -> onSessionPaused()
            }
        }
    }

    fun clearSession(ctx: Context) {
        viewModelScope.launch {
            val id = activeSession?.id
            if(id == null){
                Timber.d("Clear session called on null")
                return@launch
            }
            sensorSdk.clearCurrentWork(ctx)
            lastDashBoardData = null
            withContext(Dispatchers.IO) {
                sessionSdkRepository.deleteSessionData(id)
            }
        }
    }

    private suspend fun onSessionPaused(){
        Timber.d("onSessionPaused")
        _sessionStateFlow.emit(SessionStatePaused(getLatestSessionDashboarData()))
    }

    private suspend fun onSessionCreated(){
        Timber.d("Session CREATED")
        _sessionStateFlow.emit(SessionLoading)
        updateHasAchievement()
    }

    private suspend fun onSessionRunning(){
        Timber.d("Session active running")
        _sessionStateFlow.emit(SessionStateStarted(getLatestSessionDashboarData()))
    }

    private suspend fun onSessionStopped(){
        Timber.d("Session active stopped")
        _sessionStateFlow.emit(SessionStateStopped(getLatestSessionDashboarData()))
    }

    fun startSession(context: Context) {
        Timber.d("startSession")
        if(checkBluetoothAndGpsOrSendAlert()){
            Timber.d("check ok")

            viewModelScope.launch {
                with (_associatedSensorFlow.value) {
                    if (this != null) {
                        Timber.d("Calling sdk start")
                        updateSessionInfo()
                        SensorDetectionManager.stopNearbyDetection(context)
                        val generatedSessionId = UUID.randomUUID().toString()
                        sensorSdk.start(this.sensor, generatedSessionId, lismoveUser.uid, context)
                    } else {
                        Timber.d("No sensor associated to Lismove account")
                        _sessionStateFlow.emit(ShowSensorConfigurationPopup())
                    }
                }
            }
        } else {
            Timber.d("GPS or Bluetooth are not active")
        }
    }

    /**
     * Returns data either from latest partial exposed to UI or,
     * if it is null, builds dashboard data from local db;
     *
     * If nothing is found, returns an empty SessionDashboardData;
     */
    private suspend fun getLatestSessionDashboarData(): SessionDashBoardData {
        lastDashBoardData?.let {
            Log.e("pause_restore", "Restored ${it.time} from lastDashboardData")
            return it
        }

        val lastDashboardDataFromDb = sessionSdkRepository
            .getLastSessionPartial(activeSession!!.id)
            ?.asSessionDashBoardData(activeInitiatives)

        lastDashBoardData = lastDashboardDataFromDb ?: SessionDashBoardData()

        Log.e("pause_restore", "Restored ${lastDashboardDataFromDb?.time} from lastDashboardDataFromdb")

        return lastDashBoardData!!
    }

    private fun getSensorDataObservable(ctx: Context): LiveData<PartialSessionDataEntity?>{
        return sensorSdk.observeSensorData(ctx)
    }

    fun uploadSession(ctx: Context?){
        Timber.d("sessionStateStopped, uploadSession")
        var sessionId = activeSession!!.id
        viewModelScope.launch {
            try {
                _sessionStateFlow.emit(SessionLoading)
                val session =
                    sessionUploadUseCase.uploadSession(lismoveUser.uid, activeSession!!.id)
                _sessionStateFlow.emit(SessionUploaded(session.id!!))
            }catch (e: OutOfMemoryError) {
                System.gc()
                Timber.e(e)

                ctx?.let {
                    Toast.makeText(it, "Memoria dispositivo piena, chiudi altre app in esecuzione e prova di nuovo.", Toast.LENGTH_LONG).show()
                }

                BugsnagUtils.reportIssue(e)
            } catch (e: SessionNotValidException){
                Timber.e(e)
                isUploadingSession = false
                _sessionStateFlow.emit(SessionShowErrorDialog("Sessione troppo breve", "La sessione è troppo breve, quindi non sarà inviata al server."))
            } catch (e: Exception){
                Timber.e(e)
                delay(1000)
                isUploadingSession = false
                _sessionStateFlow.emit(SessionUploadError("Si è verificato un errore", "Grazie per aver pedalato. La sessione verrà caricata sul server appena possibile.", sessionId))
            }
        }
    }

    fun pauseSession() {
        Timber.d("pauseSession")
        if (activeSession == null) sendEvent("Nessuna sessione trovata")
        viewModelScope.launch {
            sensorSdk.pause(activeSession!!.id, true)
        }
    }

    fun resumeSession() {
        Timber.d("resumeSession")
        if(checkBluetoothAndGpsOrSendAlert()){
            if (activeSession == null) sendEvent("Nessuna sessione trovata")
            viewModelScope.launch {
                sensorSdk.resume(activeSession!!.id)
            }
        }
    }


    private fun checkBluetoothAndGpsOrSendAlert(): Boolean{
        val isBluetoothEnabled = bluetoothConnectivity.value ?: true
        val isGpsEnabled = gpsConnectivity.value ?: true

        viewModelScope.launch {
            if(!isBluetoothEnabled && !isGpsEnabled){
                _sessionStateFlow.emit(SessionShowErrorDialog("Bluetooth e GPS disabilitati","L'app ha bisogno del bluetooth e GPS per poter funzionare correttamente" ))

            }else if(!isGpsEnabled){
                _sessionStateFlow.emit(SessionShowErrorDialog("GPS disabilitato","L'app ha bisogno del GPS per poter funzionare correttamente" ))
            }
        }

        return  isGpsEnabled
    }

    fun stopSession(context: Context) {
        Timber.d("stopSession")
        if (activeSession == null) {
            sendEvent("Nessuna sessione trovata")
            return
        }

        SensorDetectionManager.startNearbyDetection(context)
        
        viewModelScope.launch {
            sensorSdk.stop(activeSession!!.id)
        }
    }

    private fun sendEvent(message: String) { _sessionEvents.value = SessionEvent(message) }

    fun initConnectedSensor() = viewModelScope.launch {
        associatedSensor = sensorRepository.getSensor(lismoveUser.uid)

        refreshSensorState()
    }

    fun refreshSensorState() {
        try {
            if (associatedSensor != null) {
                _associatedSensorFlow.value = SensorWithConnectionState(associatedSensor!!, isSensorConnected())
            } else {
                _associatedSensorFlow.value = null
            }
        }catch (e: Exception){
            sendEvent(e.localizedMessage)
        }
    }

    fun changeTheme(){
        if(theme == AppCompatDelegate.MODE_NIGHT_YES) {
            Timber.d("Theme no night")
            theme = AppCompatDelegate.MODE_NIGHT_NO
            themeRepository.setTheme( AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            Timber.d("Theme no night")
            theme = AppCompatDelegate.MODE_NIGHT_YES
            themeRepository.setTheme( AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun onPopupSent(){
        viewModelScope.launch {
            //restore the previous state
            updateSessionStateFlow(activeSession)
        }
    }

    fun getBonusList() = liveData<Lce<List<ListAlertData>>>{
        val sessionId = activeSession?.id
        if(sessionId != null){
            try{
                emit(LceLoading())
                val res = activeInitiatives.map { ListAlertData(
                    null,
                    it.settings.getBonusString(it.organization.title),
                    "x${it.settings.getActiveMultiplier(DateTimeUtils.getCurrentDate())}",
                    it.organization.title,
                ) }
                emit(LceSuccess(res))
            }catch (e: Exception){
                emit(LceError(Exception(e.localizedMessage)))
            }

        }else{
            emit(LceError(Exception("Nessuna sessione attiva")))
        }
    }

    fun getPoints(): LiveData<Lce<List<ListAlertData>>> = liveData<Lce<List<ListAlertData>>>{

        val sessionId = activeSession?.id
          if(sessionId != null){
            try{
                emit(LceLoading())
                val points = sessionSdkRepository.getPointsAndOrganizationForSession(sessionId) ?: listOf()
                var res = points.map { ListAlertData(it.organization.id.toString(), it.organization.title, it.pointEntity.points.toString()) }
                if(res.isEmpty()) res = listOf(ListAlertData(null, "Nessun progetto attivo", ""))
                emit(LceSuccess(res))
            }catch (e: Exception){
                emit(LceError(Exception(e.localizedMessage)))
            }

        }else{
            emit(LceError(Exception("Nessuna sessione attiva")))
        }
    }

    fun checkIntentExtras(extras: Bundle?, ctx: Context) = viewModelScope.launch {
        if (extras == null) return@launch

        delay(2000)
        if (sessionSdkRepository.getActiveSession() == null) {
            if (extras.containsKey(SplashScreenExtras.EXTRA_START_SESSION)) { startSession(ctx) }
        }
    }

    suspend fun isLocalPairingDone(): Boolean {
        return sensorRepository.isSensorFirstPairingDone()
    }

    fun isSensorConnected() = sensorSdk.isConnectedToSensor()
    fun reconfigureSensor(ctx: Context) {
        ctx.startActivity(Intent(ctx, DeviceConfigActivity::class.java))
    }

    fun setPauseAlertDoNotShowAgain(){
        alertPreferencesRepository.setShowManualPauseAlert(false)
    }

    fun setSessionFloatingOpen(value: Boolean) {
        tempPrefsRepository.setSessionFloatingOpen(value)
    }

    fun isSessionFloatingOpen(): Boolean {
        return tempPrefsRepository.isSessionFloatingOpen()
    }

    fun updateHasAchievement() {
        viewModelScope.launch {
            try {
                hasAchievements = achievementRepository.hasActiveAchievement(lismoveUser.uid)
                Timber.d("Has active ach is $hasAchievements")
            }catch (e: Exception){
                Timber.d("Error fetching achievement")
            }
        }
    }

    data class SensorWithConnectionState(
        val sensor: SensorEntity,
        val connected: Boolean,
    )
}

