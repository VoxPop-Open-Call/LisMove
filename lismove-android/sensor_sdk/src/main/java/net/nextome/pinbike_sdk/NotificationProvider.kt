package net.nextome.lismove_sdk

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.SessionDataEntity
import java.util.*

object NotificationProvider {

    const val NOTIFICATION_INTENT_SESSION_ID = "sessionId"
    const val NOTIFICATION_INTENT_ACTION = "action"
    const val NOTIFICATION_INTENT_ACTION_STOP = "stop"

    const val NOTIFICATION_CODE_NEW_DEVICE_DETECTED = 3
    const val NOTIFICATION_CODE_SESSION_WORKER = 42
    const val NOTIFICATION_CODE_BACKGROUND_SENSOR_DETECTION = 43

    fun getSessionInitialNotification(context: Context,
                                      workerId: UUID): Notification{
        return getBasePersistentNotification(context, workerId, "Avvio sessione", "Connessione con il sensore")
    }

    fun getBackgroundSensorDetectionInitialNotification(context: Context, workerId: UUID): Notification{
        return getBasePersistentNotification(context, UUID.randomUUID(),"LisMove è in esecuzione in background", "Sarai notificato se viene rilevato un sensore nelle vicinanze")
    }

    fun getNewDeviceFoundNotification(context: Context): Notification {
        val splashScreenIntent = Intent().apply {
            component = ComponentName(context.packageName, "it.lismove.app.android.authentication.ui.SplashScreenActivity")
            putExtra(SplashScreenExtras.EXTRA_START_SESSION, true)
        }

        return getBaseUrgentNotification(context, "Lis Move rilevato", "Tocca per iniziare una nuova sessione",
            pendingIntent = PendingIntent.getActivity(context, 1, splashScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
    }

    fun getNotificationForRunningUpdate(lastPartial: PartialSessionDataEntity,
                                        context: Context,
                                        workerId: UUID): Notification{

        return getSessionRunningNotification(lastPartial.sessionId,lastPartial, context, workerId)
    }

    fun getNotificationForSession(session: SessionDataEntity,
                                  lastPartial: PartialSessionDataEntity?,
                                  context: Context,
                                  workerId: UUID): Notification{
        return when {
            session.isRunning() -> {
                getSessionRunningNotification(session.id, lastPartial, context, workerId)
            }
            session.isStopped() -> {
                getSessionStoppedNotification(lastPartial, context, workerId)
            }
            session.isPaused() -> {
                getSessionPausedNotification(lastPartial, context, workerId)
            }
            session.isCreated() -> {
                getSessionCreatedNotification(context, workerId)
            }
            else -> {
                getBasePersistentNotification(context, workerId, "Lis Move", "")
            }
        }
    }

    private fun getSessionCreatedNotification(context: Context,
                                              workerId: UUID): Notification{
        return getBasePersistentNotification(context, workerId, "Avvio sessione", "Connessione con il sensore in corso")
    }

    private fun getSessionRunningNotification(sessionId: String,
                                              lastPartial: PartialSessionDataEntity?,
                                              context: Context,
                                              workerId: UUID): Notification{
        val bIntent = Intent(context, SessionManagerBrodcastReceiver::class.java)
        bIntent.putExtra(NOTIFICATION_INTENT_SESSION_ID, sessionId)
        bIntent.putExtra(NOTIFICATION_INTENT_ACTION, NOTIFICATION_INTENT_ACTION_STOP)

        val stopIntent: PendingIntent =
                PendingIntent.getBroadcast(context, 0, bIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val action = NotificationCompat.Action(R.drawable.ic_baseline_stop_24,"Stop", stopIntent)

        return getBasePersistentNotification(context, workerId, "Sessione in corso", getBodyFromLastPartial(lastPartial), listOf(action))
    }

    private fun getSessionStoppedNotification(lastPartial: PartialSessionDataEntity?,
                                              context: Context,
                                              workerId: UUID): Notification {

        return getBasePersistentNotification(context, workerId, "Sessione terminata", getBodyFromLastPartial(lastPartial))
    }

    private fun getSessionPausedNotification(lastPartial: PartialSessionDataEntity?,
                                             context: Context,
                                             workerId: UUID): Notification {

        return getBasePersistentNotification(context, workerId, "Sessione in pausa", getBodyFromLastPartial(lastPartial))
    }

        private fun getBodyFromLastPartial(lastPartial: PartialSessionDataEntity?): String{
        return lastPartial?.let {
            " Velocità: ${it.getAvgSpeedReadable()} Km/H " +
                    " Distanza: ${it.getTotalDistanceReadable()} Km"
        } ?: "Nessun valore ancora rilevato"
    }



    private fun getBasePersistentNotification(context: Context,
                                              workerId: UUID,
                                              title: String, body: String,
                                              actions: List<NotificationCompat.Action> = listOf()): Notification{
        val id = context.getString(R.string.notification_channel_id)


        val splashScreenIntent = Intent()

        splashScreenIntent.component = ComponentName(context.packageName, "it.lismove.app.android.authentication.ui.SplashScreenActivity")
        val pendingIntent = PendingIntent.getActivity(context, 0, splashScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createSessionNotificationChannel(context, id, context.getString(R.string.notification_channel_name))
        }


        val notification = NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification_session)
                .setOngoing(true)
                .setContentIntent(pendingIntent)

        actions.forEach {
            notification.addAction(it)
        }

        return notification.build()
    }

    private fun getBaseLowPriorityNotification(context: Context,
                                              title: String, body: String,
                                              actions: List<NotificationCompat.Action> = listOf()): Notification{
        val lowPriorityId = context.getString(R.string.notification_channel_id_lowpriority)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createSessionNotificationChannel(context, lowPriorityId, context.getString(R.string.notification_channel_name))
        }

        val notification = NotificationCompat.Builder(context, lowPriorityId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification_session)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        actions.forEach {
            notification.addAction(it)
        }

        return notification.build()
    }

    private fun getBaseUrgentNotification(context: Context,
                                          title: String, body: String,
                                          actions: List<NotificationCompat.Action> = listOf(),
                                          pendingIntent: PendingIntent? = null): Notification{
        val id = context.getString(R.string.notification_channel_id)

        val splashScreenIntent = Intent().apply {
            component = ComponentName(context.packageName, "it.lismove.app.android.authentication.ui.SplashScreenActivity")
        }

        val contentIntent = pendingIntent ?: PendingIntent.getActivity(context, 0, splashScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createImportantNotificationChannel(context, id, context.getString(R.string.notification_channel_name))
        }


        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setTicker(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification_session)
            .setOngoing(false)
            .setContentIntent(contentIntent)

        actions.forEach {
            notification.addAction(it)
        }

        return notification.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createSessionNotificationChannel(
            context: Context,
            channelId: String,
            name: String
    ): NotificationChannel {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return NotificationChannel(
                channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createImportantNotificationChannel(
        context: Context,
        channelId: String,
        name: String
    ): NotificationChannel {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_DEFAULT
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

}