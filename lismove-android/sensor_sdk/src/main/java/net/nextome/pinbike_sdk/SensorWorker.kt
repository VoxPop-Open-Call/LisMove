package net.nextome.lismove_sdk

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.*
import com.bugsnag.android.Bugsnag
import com.google.gson.Gson
import it.lismove.app.common.background_sensor_detection.SensorDetectionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_BLE
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_END
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_GPS
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_IN_PROGRESS
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_PAUSE
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_RESUME
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_SERVICE
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_SESSION
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_START
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_SYSTEM
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_CREATED
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_PAUSE_INACTIVITY
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_PAUSE_BY_USER
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_RUNNING
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_STOPPED
import net.nextome.lismove_sdk.NotificationProvider.NOTIFICATION_CODE_SESSION_WORKER
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.location.LisMoveLocationManager
import net.nextome.lismove_sdk.models.*
import net.nextome.lismove_sdk.models.LisMoveBleState.*
import net.nextome.lismove_sdk.sessionPoints.PointsManager
import net.nextome.lismove_sdk.sessionPoints.data.PointsSummary
import net.nextome.lismove_sdk.statusListener.BluetoothStatusListener
import net.nextome.lismove_sdk.statusListener.GpsStatusListener
import net.nextome.lismove_sdk.utils.BugsnagUtils
import net.nextome.lismove_sdk.utils.SensorDataManager
import net.nextome.lismove_sdk.utils.SessionDelayManager
import kotlin.coroutines.CoroutineContext
import kotlin.math.PI
import android.content.Context.ACTIVITY_SERVICE
import it.lismove.app.android.general.utils.toJson
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.database.DebugLogRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


const val KEY_INPUT_CONFIG = "baseConfig"
const val KEY_OUTPUT_WORKER_STATUS = "serviceStatus"
const val KEY_OUTPUT_LAST_DATA = "lastDataOutput"

const val GENERATE_PARTIALS_INTERVAL = 1000L

const val TAG = "LisMoveSession"
class SensorWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    //private val userId: LisMoveUser? = getKoin().getProperty<LisMoveUser>("PIN_BIKE_USER")

    private val sessionRepository: SessionSdkRepository by inject()
    private val locationManager: LisMoveLocationManager by inject()
    private val sensorSdk: LismoveSensorSdk by inject()
    private val bleManager: LismoveBleManager by inject()

    private val sensorDetectionRepository: SensorDetectionRepository by inject()
    private val debugLogRepository: DebugLogRepository by inject()

    // points
    private val pointsManager: PointsManager by inject()
    private var lastPointSummary: PointsSummary? = null

    private var savePartialsDelayInMillis = SessionDelayManager.getDelay(applicationContext).toLong() * 1000

    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var workerConfig: WorkerConfiguration

    private var currentPosition: LisMoveGpsPosition? = null

    private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // last cadence received by sensor
    private var currentCadenceEntity: CadencePrimitive? = null

    private var wheelCircunferenceInMm: Double = 2111.0 // mm circunference
    private var hubCoefficient: Double = 1.0

    private var startSensorBattery: Int = -1
    private var sensorFirmwareVersion: String = ""

    private var hasWorkToDo = true
    private var running = false

    private var isFirstStart = true

    // First partial needs to be setted at PARTIAL_TYPE_START
    private var isFirstPartial = true

    private var hasEverConnectedToSensor = false

    private var lastSavedPartial: PartialSessionDataEntity? = null
    private var lastGeneratedPartial: PartialSessionDataEntity? = null

    private fun addToPartialCache(partial: PartialSessionDataEntity?) {
        try {
            if (partial == null) return
            lastGeneratedPartial = partial
            partialCache.add(partial)
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            e.printStackTrace()
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker is started")
        debugLogRepository.addSessionLogAsync("Worker started")

        try {
            workerConfig = Gson().fromJson(inputData.getString(KEY_INPUT_CONFIG), WorkerConfiguration::class.java)

            workerConfig.device?.let {
                wheelCircunferenceInMm = it.wheelDiameter * PI
                hubCoefficient = it.hubCoefficient
            }

            if (workerConfig.device == null) {
                onHasServiceError("Impossibile inizializzare la sessione (CODE 3)", true)
            }

        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage ?: "No error message provided")
            e.printStackTrace()
            onHasServiceError("Impossibile inizializzare la sessione: ${e.localizedMessage}", true)
        }

        // Mark the Worker as important
        setForeground(createForegroundInfo())

        if (initialize()) {
            checkForCrashedSessions()
            cleanDebugLog()
            checkBleConnection(currentCoroutineContext())
            observeGpsEnabledOrDisabled(currentCoroutineContext())
            observeBluetoothEnabledOrDisabled(currentCoroutineContext())
            observeBle(currentCoroutineContext())
            observeAndValidateSessionChanges(currentCoroutineContext())
            startSession()
            generatePartialsTask(currentCoroutineContext())
            updatePartialsTask(currentCoroutineContext())
            observeGpsLocationChanges(currentCoroutineContext())
            observeSessionActive(currentCoroutineContext())
            updatePoints(currentCoroutineContext())
            observeDataManagerErrors(currentCoroutineContext())
            logInitialDebugInfo(applicationContext)
            logPeriodicDebugInfo(currentCoroutineContext())

            while (hasWorkToDo){
                //Check keepalive each sec
                delay(1000)
            }

            // Session is just finished, snooze detection notifications for a bit
            sensorDetectionRepository.setLastSensorDetectionTs(System.currentTimeMillis())

            try {
                sessionRepository.updateStatus(
                    workerConfig.sessionId,
                    SESSION_STATUS_STOPPED,
                )
            } catch (e: Exception) {
                // if the session was deleted because too short,
                // there's nothing to update
            }

            Log.e(TAG,"Sensor Worker is cancelling itself")
            currentCoroutineContext().cancel()
            // disconnectBle()
            return Result.success()
        } else {
            onHasServiceError("Impossibile inizializzare la sessione (CODE 4)", true)
            delay(10000) // Wait for cleanup
            currentCoroutineContext().cancel()
            // disconnectBle()
            return Result.failure()
        }
    }

    private suspend fun cleanDebugLog() {
        try {
            debugLogRepository.deleteOldEntries()
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
        }
    }

    private suspend fun checkForCrashedSessions() {
        val activeSession = sessionRepository.getActiveSession()

        if (activeSession == null) return

        // A previous session was running
        if (activeSession.isInProgress()) {
            val activeSessionPartials = sessionRepository.getPartials(activeSession.id)
            if (activeSessionPartials == null) {
                BugsnagUtils.logEvent("Detected session hasn't any partial, IGNORING")
                addDebugPartial(PARTIAL_TYPE_SERVICE, "Detected crashed session. Ignoring because no partial associated were found.")
                return
            } else {
                val partialNumber = activeSessionPartials.size

                if (partialNumber < 10) {
                    addDebugPartial(PARTIAL_TYPE_SERVICE, "Detected crashed session. Not recovering because partials were too few ($partialNumber).")
                    BugsnagUtils.logEvent("Detected crashed session (ignoring because partials were too few ($partialNumber))")
                } else {
                    addDebugPartial(PARTIAL_TYPE_SERVICE, "Detected crashed session. Recovering data from last partial ($partialNumber partials saved)")
                    BugsnagUtils.logEvent("Detected crashed session (RESUMING DATA FROM LAST PARTIAL ($partialNumber partials)")

                    val result = sensorDataManager.recoverFromSessionPartials(activeSessionPartials)
                    isFirstPartial = false
                    sensorDataManager.setPauseOnGpsInactivityEnabled(30)

                    if (!result) {
                        addDebugPartial(PARTIAL_TYPE_SERVICE, "Detected crashed session. Closing session because no last partial was saved.")
                        onHasServiceError(
                            "Detected crashed session (FORCING CLOSING because no last partial were saved)",
                            true
                        )
                    }
                }
            }
        }

        Log.e("activeSession", "WORK ACTIVE IS ${activeSession != null}")
    }

    private fun updatePoints(context: CoroutineContext) {
        CoroutineScope(context).launch {
            while (hasWorkToDo) {
                lastSavedPartial?.let { partial ->
                    currentPosition?.let { position ->
                        lastPointSummary = pointsManager.updatePoints(
                            partial.getTotalDistance(),
                            position.latitude,
                            position.longitude,
                            partial.timestamp
                        )
                    }
                }

                delay(GENERATE_PARTIALS_INTERVAL)
            }
        }
    }

    private suspend fun startSession() {
        if (sessionRepository.getSessionById(workerConfig.sessionId) != null) {
            sessionRepository.updateProposedStatus(
                workerConfig.sessionId,
                SESSION_STATUS_RUNNING,
            )
        } else {
            sessionRepository.addSessionOrIgnore(
                SessionDataEntity(
                    id = workerConfig.sessionId,
                    proposedStatus = SESSION_STATUS_CREATED,
                    userId = workerConfig.userId,
                    startTime = System.currentTimeMillis()
                ))
        }
    }

    private fun checkBleConnection(context: CoroutineContext) {
        CoroutineScope(context).launch {
            while (hasWorkToDo) {
                if (sensorDataManager.isSessionActive) {
                    connectBleIfDisconnected()
                }

                // check ble connection each 15 secs
                delay(15000)
            }
        }
    }


    private suspend fun connectBleIfDisconnected() {
        // check connection state
        workerConfig.device?.let {
            try {
                sensorSdk.scanAndConnectToSensorIfNecessary(
                    it.uuid,
                    wheelCircunferenceInMm.toFloat(),
                    hubCoefficient
                )
            } catch (e: Exception) {
                onHasServiceError("Unable to connect: ${e.message}", false)
            }
        }
    }

    private fun observeSessionActive(currentCoroutineContext: CoroutineContext) {
        CoroutineScope(currentCoroutineContext).launch {
            sensorDataManager.isSessionActiveObservable().collect { active ->
                if (active) {
                    if (!isFirstStart) {
                        // Skip first partial for a more precise GPS lock
                        delay(1500)
                        saveNewPartialToDb(PARTIAL_TYPE_RESUME)
                        debugLogRepository.addSessionLogAsync("Resumed")
                    } else {
                        isFirstStart = false
                    }

                    locationManager.resumeLocation()
                    sensorSdk.resume(workerConfig.sessionId)
                } else {
                    sensorSdk.pause(workerConfig.sessionId, false)

                    debugLogRepository.addSessionLogAsync("Paused: ${sensorDataManager.getLastPauseReason()}")
                    addDebugPartial(PARTIAL_TYPE_SESSION, sensorDataManager.getLastPauseReason())

                    saveNewPartialToDb(PARTIAL_TYPE_PAUSE)
                    locationManager.pauseLocation()
                }
            }
        }
    }

    /**
     * Reports a FATAL error in the worker.
     * - The app module will be notified and the message will be shown to the user a explaination.
     * - Worker will be marked as "done" and it will start cleaning up and cancelling running jobs.
     * - Current session will be marked as STOPPED.
     */
    private suspend fun onHasServiceError(message: String, isFatal: Boolean) {
        Log.e(TAG, "Service error: $message")
        if (isFatal) reportIssue(Exception(message))

        if (isFatal) {
            emitServiceStatus(LisMoveServiceState(LisMoveServiceStatus.FATAL_ERROR, message))

            val currentSession = sessionRepository.getSessionById(workerConfig.sessionId)

            if (currentSession != null) {
                if (currentSession.status == SESSION_STATUS_CREATED) {
                    sessionRepository.deleteSessionData(workerConfig.sessionId)
                } else {
                    sensorDataManager.emptyCache()

                    // recover sensor battery
                    startSensorBattery = currentSession.startBattery ?: -1
                    sensorDataManager.lastSensorBattery =  currentSession.endBattery ?: currentSession.startBattery ?: -1

                    // recover firmware version
                    sensorFirmwareVersion = currentSession.firmwareVersion ?: ""

                    val lastRecoveredPartial = sessionRepository.getLastSessionPartial(currentSession.id)

                    // In case of crash, sensorDataManger has not accurate data
                    // take data from last partial
                    sessionRepository.updateSession(
                        currentSession.copy(
                            status = SESSION_STATUS_STOPPED,
                            proposedStatus = null,
                            endTime = System.currentTimeMillis(),
                            gyroDistance = lastRecoveredPartial?.gyroDistance,
                            gpsOnlyDistance = lastRecoveredPartial?.gpsDistance,
                            totalKm = lastRecoveredPartial?.gyroDistance)
                    )
                }
            }

            hasWorkToDo = false
        } else {
            emitServiceStatus(LisMoveServiceState(LisMoveServiceStatus.GENERIC_ERROR, message))
        }
    }

    @SuppressLint("MissingPermission")
    private fun observeGpsLocationChanges(currentCoroutineContext: CoroutineContext) {
        CoroutineScope(currentCoroutineContext).launch {
            locationManager.locationObservable(currentCoroutineContext, applicationContext).collect {
                if (it != null) {
                    currentPosition = it
                }
            }
        }
    }

    val phoneStartBattery = BatteryManager().getBatteryLevel(applicationContext)

    private fun observeAndValidateSessionChanges(currentCoroutineContext: CoroutineContext) {
        CoroutineScope(currentCoroutineContext).launch {
            sessionRepository.getSessionByIdObservable(workerConfig.sessionId).collect { session ->

                if (session == null) {
                    Bugsnag.leaveBreadcrumb("Service running while session is deleted.")
                    return@collect
                }

                notificationManager.notify(42, NotificationProvider.getNotificationForSession(session, lastSavedPartial, applicationContext, id))

                if (session.proposedStatus != null) {
                    BugsnagUtils.logEvent("Proposed session status ${session.proposedStatus}")

                    Log.i(TAG, "Proposed session status update: ${session.proposedStatus} with status ${session.status}")

                    when (session.proposedStatus) {
                        SESSION_STATUS_CREATED -> {
                            BugsnagUtils.logEvent("Session CREATED")

                            sessionRepository.updateSession(session.copy (
                                status = SESSION_STATUS_CREATED,
                                proposedStatus = null,
                                phoneStartBattery = phoneStartBattery,
                                hubCoefficient = hubCoefficient
                            ).apply {
                                if (session.firmwareVersion == null) {
                                    firmwareVersion = sensorFirmwareVersion
                                }

                                if (session.startBattery != null || session.startBattery != -1) {
                                    startBattery = startSensorBattery
                                }
                            })

                            locationManager.resumeLocation()
                            sensorDataManager.resumeFromPause()
                        }

                        SESSION_STATUS_RUNNING -> {
                            BugsnagUtils.logEvent("Session RUNNING")

                            running = true

                            locationManager.resumeLocation()
                            sensorDataManager.resumeFromPause()
                            sessionRepository.updateSession(session.copy (
                                status = SESSION_STATUS_RUNNING,
                                proposedStatus = null,
                                phoneStartBattery = phoneStartBattery,
                                hubCoefficient = hubCoefficient
                            ).apply {
                                if (session.firmwareVersion == null) {
                                    firmwareVersion = sensorFirmwareVersion
                                }

                                if (session.startBattery != null) {
                                    startBattery = startSensorBattery
                                }
                            })
                        }

                        SESSION_STATUS_PAUSE_INACTIVITY -> {
                            BugsnagUtils.logEvent("Session PAUSED (inactivity)")
                            sensorDataManager.emptyCache()

                            running = false

                            sessionRepository.updateSession(session.copy(
                                status = SESSION_STATUS_PAUSE_INACTIVITY,
                                endTime = System.currentTimeMillis(),
                                proposedStatus = null,
                                endBattery = sensorDataManager.lastSensorBattery,
                                phoneEndBattery = BatteryManager().getBatteryLevel(applicationContext),
                                hubCoefficient = hubCoefficient
                            ).apply {
                                if (session.firmwareVersion == null) {
                                    firmwareVersion = sensorFirmwareVersion
                                }

                                if (session.startBattery != null) {
                                    startBattery = startSensorBattery
                                }
                            })
                        }

                        SESSION_STATUS_PAUSE_BY_USER -> {
                            BugsnagUtils.logEvent("Session PAUSED (by user)")
                            sensorDataManager.emptyCache()

                            running = false
                            sensorDataManager.forcePause()
                            locationManager.pauseLocation()
                            currentPosition = null

                            sessionRepository.updateSession(session.copy(
                                status = SESSION_STATUS_PAUSE_BY_USER,
                                endTime = System.currentTimeMillis(),
                                proposedStatus = null,
                                endBattery = sensorDataManager.lastSensorBattery,
                                phoneEndBattery = BatteryManager().getBatteryLevel(applicationContext),
                                hubCoefficient = hubCoefficient
                            ).apply {
                                if (session.firmwareVersion == null) {
                                    firmwareVersion = sensorFirmwareVersion
                                }

                                if (session.startBattery != null) {
                                    startBattery = startSensorBattery
                                }
                            })
                        }

                        SESSION_STATUS_STOPPED -> {
                            BugsnagUtils.logEvent("Session STOPPED")

                            // Try last time to connect to sensor
/*                            if (isGpsOnlyMode) {
                                connectBleIfDisconnected()
                            }*/

                            running = false

                            sensorDataManager.emptyCache()

                            Log.e("gpsData", "End session gpsOnlyDistance: ${sensorDataManager.totalGpsCacheDistanceInKm}")

                            sessionRepository.updateSession(session.copy(
                                status = SESSION_STATUS_STOPPED,
                                proposedStatus = null,
                                endTime = System.currentTimeMillis(),
                                gyroDistance = sensorDataManager.totalGyroDistanceInKm,
                                totalKm = sensorDataManager.getTotalDistance(),
                                endBattery = sensorDataManager.lastSensorBattery,
                                phoneEndBattery = BatteryManager().getBatteryLevel(applicationContext),
                                nationalPoints = lastSavedPartial?.nationalPoints,
                                gpsOnlyDistance = sensorDataManager.totalGpsOnlyDistanceInKm,
                                duration = sensorDataManager.sessionElapsedTimeInSec.toLong(),
                                hubCoefficient = hubCoefficient
                            ).apply {
                                if (session.firmwareVersion == null) {
                                    firmwareVersion = sensorFirmwareVersion
                                }

                                if (session.startBattery != null) {
                                    startBattery = startSensorBattery
                                }
                            })

                            saveNewPartialToDb(PARTIAL_TYPE_END)
                            hasWorkToDo = false
                        }
                    }
                }
            }
        }
    }

    private fun updatePartialsTask(currentCoroutineContext: CoroutineContext) {
        CoroutineScope(currentCoroutineContext).launch {
            while (true){
                if (running) {
                   saveNewPartialToDb(PARTIAL_TYPE_IN_PROGRESS)
                }

                delay(savePartialsDelayInMillis)
            }
        }
    }

    /**
     * Cache of partials that aren't saved on DB.
     * Ex. if db save time is 3 seconds, the saved partial will have
     * the delta sums of the 3 previous partials
     */
    val partialCache = arrayListOf<PartialSessionDataEntity>()
    private fun emptyPartialCache() {
        try {
            partialCache.clear()
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            e.printStackTrace()
        }
    }

    private suspend fun saveNewPartialToDb(type: Int) {
        try {
            if (currentPosition == null) {
                currentPosition = locationManager.awaitLastLocationOrNull(applicationContext)
            }

            if (lastSavedPartial == null) {
                val partial = PartialSessionDataEntity(
                    timestamp = System.currentTimeMillis(),
                    latitude = currentPosition?.latitude ?: lastSavedPartial?.latitude,
                    longitude = currentPosition?.longitude ?: lastSavedPartial?.longitude,
                    altitude = currentPosition?.altitude ?: lastSavedPartial?.altitude,
                    sessionId = workerConfig.sessionId,
                    batteryLevel = sensorDataManager.lastSensorBattery,
                    type = type,
                    totalGyroDistanceInKm = sensorDataManager.totalGyroDistanceInKm,
                    totalGpsOnlyDistanceInKm = sensorDataManager.totalGpsOnlyDistanceInKm,
                    totalGpsCacheDistanceInKm = sensorDataManager.totalGpsCacheDistanceInKm,
                    sessionElapsedTimeInSec = sensorDataManager.sessionElapsedTimeInSec,
                )

                if (isFirstPartial) {
                    partial.type = PARTIAL_TYPE_START
                    isFirstPartial = false
                }

                lastSavedPartial = partial
                sessionRepository.addPartial(partial)
                emptyPartialCache()

                Log.i(
                    "LisMoveLocation",
                    "Adding GPS partial (${currentPosition?.latitude}, ${currentPosition?.longitude})"
                )
            } else {
                lastSavedPartial?.let { lstSavedPartial ->
                    val partial = (lastGeneratedPartial ?: lstSavedPartial).copy(
                        timestamp = System.currentTimeMillis(),
                        latitude = currentPosition?.latitude ?: lstSavedPartial.latitude,
                        longitude = currentPosition?.longitude ?: lstSavedPartial.longitude,
                        altitude = currentPosition?.altitude ?: lstSavedPartial.altitude,
                        type = type,
                        totalGyroDistanceInKm = sensorDataManager.totalGyroDistanceInKm,
                        totalGpsOnlyDistanceInKm = sensorDataManager.totalGpsOnlyDistanceInKm,
                        totalGpsCacheDistanceInKm = sensorDataManager.totalGpsCacheDistanceInKm,
                        sessionElapsedTimeInSec = sensorDataManager.sessionElapsedTimeInSec,
                    )

                    partial.deltaRevs = 0
                    partial.gyroDeltaDistance = 0.0

                    // gyroDeltaDistance and deltaRevs are NULL in GPS partials
                    // If there's at least one GPS partial, report null as partial sum
                    if (partialCache.any { it.gyroDeltaDistance == null }) {
                        partial.deltaRevs = null
                        partial.gyroDeltaDistance = null
                    } else {
                        partialCache.forEach {
                            partial.deltaRevs = (partial.deltaRevs ?: 0) + (it.deltaRevs ?: 0)
                            partial.gyroDeltaDistance =
                                (partial.gyroDeltaDistance ?: 0.0) + (it.gyroDeltaDistance ?: 0.0)
                        }
                    }


                    if (isFirstPartial) {
                        partial.type = PARTIAL_TYPE_START
                        isFirstPartial = false
                    }

                    emptyPartialCache()
                    lastSavedPartial = partial
                    sessionRepository.addPartial(partial)

                    Log.i(
                        "LisMoveLocation",
                        "Adding partial (${currentPosition?.latitude}, ${currentPosition?.longitude}"
                    )
                    Log.i(
                        "LisMoveLocation",
                        "Gps distance ${partial.gpsDistance}, Gyro distance ${partial.gyroDistance})"
                    )
                }
            }
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
            e.printStackTrace()
        }
    }

    private fun generatePartialsTask(currentCoroutineContext: CoroutineContext) {
        CoroutineScope(currentCoroutineContext).launch {
            Log.d(TAG, "Starting partial generation task")

            while (true) {
                try {
                    val generatedPartial = if (sensorDataManager.isGpsOnlyMode) {

                        if (!sensorDataManager.isSessionActive) {
                            delay(GENERATE_PARTIALS_INTERVAL)
                            continue
                        }

                        sensorDataManager.getPartial(currentPosition)
                    } else if (currentCadenceEntity != null) {
                        sensorDataManager.getPartial(currentCadenceEntity!!)
                    } else {
                        sensorDataManager.getPartial(currentPosition)
                    }


                    if (generatedPartial != null) {
                        //The worker take some time to stop, don't update room if the session was stopped
                        if (running) {

                            with(generatedPartial) {
                                nationalPoints = lastPointSummary?.nationalPoints ?: 0
                                initiativePoints = lastPointSummary?.initiativePoints ?: 0
                                urban = pointsManager.isLocationUrban(
                                    currentPosition?.latitude,
                                    currentPosition?.longitude
                                )
                                addToPartialCache(this)
                            }

                            emitUiUpdate(generatedPartial)

                            notificationManager.notify(
                                42, NotificationProvider
                                    .getNotificationForRunningUpdate(
                                        generatedPartial,
                                        applicationContext,
                                        id
                                    )
                            )
                        }
                    } else {
                        // Generated partial is null, emit UI update only to increment elapsed time
                        emitUiUpdate(sensorDataManager.getEmptyPartial())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("SensorWorker", e?.localizedMessage ?: "")
                }

                delay(GENERATE_PARTIALS_INTERVAL)
            }
        }
    }

    /**
     * Send an update to app module observers with a new partial to update the dashboard UI;
     */
    private fun emitUiUpdate(partial: PartialSessionDataEntity) {
        val sensorDataSerialized = Gson().toJson(partial)

        setProgressAsync(workDataOf(
            KEY_OUTPUT_LAST_DATA to sensorDataSerialized,
        ))
    }

    /**
     * Send app module a notification with a status update.
     * FATAL_ERROR error messages are shown and readable by end users
     */
    private fun emitServiceStatus(serviceState: LisMoveServiceState) = CoroutineScope(Dispatchers.Default).launch {
        val statusSerialized = Gson().toJson(serviceState)
        setProgressAsync(workDataOf(KEY_OUTPUT_WORKER_STATUS to statusSerialized))
    }

    private suspend fun initialize(): Boolean {
        Log.d(TAG, "Initializing the Worker")
        sensorDataManager = SensorDataManager(wheelCircunferenceInMm, workerConfig.sessionId, hubCoefficient)
        bleManager.setWheelCircunferenceInMm(wheelCircunferenceInMm.toFloat())
        bleManager.setHubCoefficient(hubCoefficient)
        pointsManager.initManager(workerConfig.userId, workerConfig.sessionId)

        return true
    }

    fun observeBle(coroutineContext: CoroutineContext) {
        CoroutineScope(coroutineContext).launch {
            bleManager.cadenceObserver.collect { cadence ->
                currentCadenceEntity = cadence
            }
        }

        CoroutineScope(coroutineContext).launch {
            bleManager.batteryObservable.collect { battery ->
                if (startSensorBattery == -1) {
                    startSensorBattery = battery
                }

                sensorDataManager.lastSensorBattery = battery
            }
        }

        CoroutineScope(coroutineContext).launch {
            sensorSdk.observeBleStatus().collect {
                when (it) {
                    BLE_CONNECTING -> { addDebugPartial(PARTIAL_TYPE_BLE, "BLE Connecting") }
                    BLE_CONNECTED -> { addDebugPartial(PARTIAL_TYPE_BLE, "BLE Connected") }
                    BLE_READY -> {
                        if (sensorFirmwareVersion.isBlank()) {
                            sensorFirmwareVersion = bleManager.getSoftwareRevision()
                        }

                        sensorDataManager.isGpsOnlyMode = false
                        addDebugPartial(PARTIAL_TYPE_BLE, "BLE READY")
                        addDebugPartial(PARTIAL_TYPE_BLE, """
                            Checking sensor versions... Software: ${bleManager.getSoftwareRevision()}, Firmware: ${bleManager.getFirmwareRevision()}, Hardware: ${bleManager.getHardwareRevision()} 
                            """.trim())

                        // TODO if sensor is connected again, force resume
                        sensorDataManager.resumeFromPause()

                        hasEverConnectedToSensor = true
                    }
                    BLE_DISCONNECTING -> { addDebugPartial(PARTIAL_TYPE_BLE, "BLE Disconnecting") }
                    BLE_DISCONNECTED -> {
                        sensorDataManager.isGpsOnlyMode = true
                        onHasServiceError("Sensore disconnesso (CODE 5)", false)
                        addDebugPartial(PARTIAL_TYPE_BLE, "BLE Disconnected")



                        // If latest GPS delta are 0, automatically pause session
                        // Ignore GPS pause again if still running after 30 secs
                        if (hasEverConnectedToSensor) {
                            sensorDataManager.setPauseOnGpsInactivityEnabled(30)
                        }
                    }
                    BLE_FAILED_TO_CONNECT -> {
                        sensorDataManager.isGpsOnlyMode = true
                        onHasServiceError("Impossibile connettersi al sensore (CODE 6)", false)
                        addDebugPartial(PARTIAL_TYPE_BLE, "BLE Failed to connect")
                    }
                }
            }
        }
    }

    private fun observeGpsEnabledOrDisabled(coroutineContext: CoroutineContext) {
        CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
            var wasDisabledBefore = false
            GpsStatusListener(applicationContext).asFlow().collect { enabled ->
                if (enabled) {
                    if (wasDisabledBefore) addDebugPartial(PARTIAL_TYPE_SYSTEM, "GPS enabled by user")
                } else {
                    wasDisabledBefore = true
                    sensorDataManager.forcePause()
                    locationManager.pauseLocation()
                    currentPosition = null

                    addDebugPartial(PARTIAL_TYPE_SYSTEM, "GPS disabled by user")
                }
            }
        }
    }

    private fun observeBluetoothEnabledOrDisabled(coroutineContext: CoroutineContext) {
        CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
            var wasDisabledBefore = false
            BluetoothStatusListener(applicationContext).asFlow().collect { enabled ->
                if (enabled) {
                    if (wasDisabledBefore) addDebugPartial(PARTIAL_TYPE_SYSTEM, "Bluetooth enabled by user")
                } else {
                    wasDisabledBefore = true
                    addDebugPartial(PARTIAL_TYPE_SYSTEM, "Bluetooth disabled by user")
                }
            }
        }
    }

    private fun logInitialDebugInfo(context: Context) {
        try {
            // Log Power Save
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            addDebugPartial(PARTIAL_TYPE_SYSTEM, "Power Save enabled: ${powerManager.isPowerSaveMode}")

            // Log low memory devices
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            if (activityManager.isLowRamDevice) { addDebugPartial(PARTIAL_TYPE_SYSTEM, "Lis Move running on a Low Ram Device") }

            // Low current low memory
            val mi = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(mi)
            if (mi.lowMemory) { addDebugPartial(PARTIAL_TYPE_SYSTEM, "Low memory alert received") }
        } catch (e: Exception) {
            BugsnagUtils.reportIssue(e)
        }
    }

    private fun logPeriodicDebugInfo(coroutineContext: CoroutineContext) {
        val PERIODIC_DEBUG_INFO_DELAY = 60L * 3L * 1000L // 3 minutes

        CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
            while (true) {
                if (running) {
                    val debugInfo = locationManager.getDebugInfo()
                    addDebugPartial(PARTIAL_TYPE_GPS, debugInfo.toJson())

                    if (sensorDataManager.lastSensorBattery != -1) {
                        addDebugPartial(
                            PARTIAL_TYPE_BLE,
                            "Last sensor battery: ${sensorDataManager.lastSensorBattery}"
                        )
                    }
                }

                delay(PERIODIC_DEBUG_INFO_DELAY)
            }
        }
    }

    private fun observeDataManagerErrors(coroutineContext: CoroutineContext) {
        CoroutineScope(coroutineContext).launch(Dispatchers.IO) {
            sensorDataManager.errorFlow.collect { error ->
                error?.let { addDebugPartial(PARTIAL_TYPE_SESSION, it) }
            }
        }
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(): ForegroundInfo {
       val notification = NotificationProvider.getSessionInitialNotification(applicationContext, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_CODE_SESSION_WORKER, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(NOTIFICATION_CODE_SESSION_WORKER, notification)
        }
    }

    private fun disconnectBle() {
        if (sensorSdk.isConnectedToSensor()) {
            bleManager.disconnect().await()
            bleManager.close()
        }
    }

    fun reportIssue(e: Throwable){
        BugsnagUtils.reportWorkerIssue(e, workerConfig.sessionId, workerConfig.device?.uuid, workerConfig.device?.name)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addDebugPartial(type: Int, extra: String){
        // Debug partials may be reported when service is terminating
        // Run in global scope to assure debug writes are not canceled
        GlobalScope.launch(Dispatchers.IO) {
            sessionRepository.addDebugPartial(workerConfig.sessionId, type, extra)
        }
    }
}