package it.lismove.app.android.background_detect

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import it.lismove.app.common.PreferencesKeys
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.deviceConfiguration.repository.SensorRepository
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SensorEntity
import kotlinx.coroutines.delay
import net.nextome.lismove_sdk.NotificationProvider
import net.nextome.lismove_sdk.NotificationProvider.NOTIFICATION_CODE_BACKGROUND_SENSOR_DETECTION
import net.nextome.lismove_sdk.LismoveSensorSdk
import it.lismove.app.common.background_sensor_detection.SensorDetectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.nextome.lismove_sdk.database.SessionSdkRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*

const val NOTIFICATION_SNOOZE_INTERVAL = 60000 * 5 // 5 mins

class SensorDetectionWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KoinComponent {

    private val dataStore: DataStore<Preferences> by inject()
    private val authRepository: AuthRepository by inject()
    private val userRepository: UserRepository by inject()
    private val sensorRepository: SensorRepository by inject()
    private val sensorSdk: LismoveSensorSdk by inject()
    private val sessionSdkRepository: SessionSdkRepository by inject()
    private val sensorDetectionRepository: SensorDetectionRepository by inject()


    private val SCANNER_DELAY_TIME = 15000L
    private val SCANNER_TIMEOUT = 30000L

    private var hasEnabledService = true
    private var userId: String? = null
    private var user: LisMoveUser? = null
    private var associatedSensor: SensorEntity? = null


    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        while (hasEnabledService) {
            delay(SCANNER_DELAY_TIME)

            try {
                if (!isAuthenticated()) continue
                if (!checkUserId()) continue
                if (!checkUser()) continue
                if (!checkAssociatedSensor()) continue

                if (isAppInForeground()) {
                    Timber.d("Scan pause with app in foreground")
                } else {
                    if (sessionIsNotRunning()) {
                        if (hasSensorNearby()) {
                            showNotificationOrStartSession()
                        } else {
                            Timber.d("Background Service: Sensor not found")
                        }
                    } else {
                        return Result.failure()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e.localizedMessage)
                e.printStackTrace()
            }
        }

        return Result.success()
    }

    private suspend fun sessionIsNotRunning() = sessionSdkRepository.getActiveSession() == null
    private suspend fun isAppInForeground(): Boolean = dataStore.data.map { it[PreferencesKeys.PREF_IS_APP_FOREGROUND] }.first() ?: true
    private suspend fun isAutoStartEnabled(): Boolean = dataStore.data.map { it[PreferencesKeys.PREF_IS_BACKGROUND_SENSOR_DETECTION_AUTO_START_ENABLED] }.first() ?: false

    private suspend fun showNotificationOrStartSession() {
        suspend fun sendNotificationOrStartSession() {
            if (isAutoStartEnabled()) {
                if (associatedSensor != null && userId != null) {
                    SensorDetectionManager.stopNearbyDetection(applicationContext)
                    val generatedSessionId = UUID.randomUUID().toString()
                    sensorSdk.start(associatedSensor!!, generatedSessionId, userId!!, applicationContext)
                }
            } else {
                with(applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
                    notify(NotificationProvider.NOTIFICATION_CODE_NEW_DEVICE_DETECTED,
                        NotificationProvider.getNewDeviceFoundNotification(applicationContext))
                }
            }
        }

        val lastNotificationTs = sensorDetectionRepository.getLastSensorDetectionTs()

        if (lastNotificationTs == 0L) {
            sendNotificationOrStartSession()
            sensorDetectionRepository.setLastSensorDetectionTs(System.currentTimeMillis())
        } else if (System.currentTimeMillis() >= (lastNotificationTs ?: 0L) + NOTIFICATION_SNOOZE_INTERVAL) {
            sendNotificationOrStartSession()
            sensorDetectionRepository.setLastSensorDetectionTs(System.currentTimeMillis())
        } else {
            Timber.d("Nearby sensor detected. Notification not send because SNOOZE time (Last notification sent on $lastNotificationTs, current time is ${System.currentTimeMillis()})")
        }
    }

    private suspend fun hasSensorNearby(): Boolean {
        val detectedSensor = sensorSdk.scanAndReturnDevice(
            macAddress = associatedSensor?.uuid,
            timeout = SCANNER_TIMEOUT
        )

        return detectedSensor != null
    }

    private suspend fun checkUserId(): Boolean {
        if (userId == null) { userId = authRepository.getUserUid() }
        return userId != null
    }

    private suspend fun checkUser(): Boolean {
        if (user == null) {
            user = userRepository.getCachedUserProfile(userId!!)
        }

        return user != null
    }

    private suspend fun checkAssociatedSensor(): Boolean {
        if (associatedSensor == null) {
            associatedSensor = sensorRepository.getLocalCachedSensor(userId!!)
        }

        return associatedSensor != null
    }

    private suspend fun isAuthenticated() = authRepository.isUserLogIn()

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationProvider.getBackgroundSensorDetectionInitialNotification(applicationContext, id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_CODE_BACKGROUND_SENSOR_DETECTION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(NOTIFICATION_CODE_BACKGROUND_SENSOR_DETECTION, notification)
        }
    }
}