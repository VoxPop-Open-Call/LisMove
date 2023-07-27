package net.nextome.lismove_sdk.sensorUpgrade

import android.app.Activity
import androidx.core.app.NotificationCompat
import net.nextome.lismove_sdk.BuildConfig
import no.nordicsemi.android.dfu.DfuBaseService

class LismoveDfuService: DfuBaseService() {
    override fun getNotificationTarget(): Class<out Activity> {
        return DeviceUpgradeActivity::class.java
    }

    override fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    override fun updateForegroundNotification(builder: NotificationCompat.Builder) {
        super.updateForegroundNotification(builder)
    }
}