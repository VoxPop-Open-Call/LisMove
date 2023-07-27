package net.nextome.lismove_sdk.receiver

import android.app.NotificationManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import net.nextome.lismove_sdk.NotificationProvider
import net.nextome.lismove_sdk.NotificationProvider.NOTIFICATION_CODE_NEW_DEVICE_DETECTED
import it.lismove.app.room.entity.SessionDataEntity.Companion.SESSION_STATUS_RUNNING
import net.nextome.lismove_sdk.database.SessionSdkRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class NewDeviceFoundReceiver: BroadcastReceiver(), KoinComponent {
    private val sessionRepository: SessionSdkRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "net.nextome.lismove_sdk.DEVICE_FOUND") {
            if (intent.hasExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT)) {

                try {
                val results: List<ScanResult> =
                    intent.getParcelableArrayListExtra("android.bluetooth.le.extra.LIST_SCAN_RESULT") ?: listOf()

                if (results.isEmpty()) return

                    runBlocking {
                        val activeSession = sessionRepository.getActiveSession()

                        if (activeSession == null) {
                            // Send a notification only if there's not an active session
                            sendNotification(context)
                        } else if (activeSession.status != SESSION_STATUS_RUNNING) {
                            // or the current session is paused/stopped
                            sendNotification(context)
                        }
                    }
                } catch (e: Exception) { }
            }
        }
    }

    fun sendNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            NOTIFICATION_CODE_NEW_DEVICE_DETECTED,
            NotificationProvider.getNewDeviceFoundNotification(context)
        )
    }
}