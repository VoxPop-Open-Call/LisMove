package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity
data class PartialSessionDataEntity(
    @PrimaryKey
    var timestamp: Long,

    val sessionId: String,

    var altitude: Double?,
    var latitude: Double?,
    var longitude: Double?,

    var deltaRevs: Long? = null,
    val wheelTime: Int = 0,

    val speed: Double = 0.0,
    var gyroDistance: Double = 0.0,
    var gpsDistance: Double = 0.0,
    // is null if gps partial
    var gyroDeltaDistance: Double? = null,
    val elapsedTimeInMillis: Long = 0L,
    val averageSpeed: Double = 0.0,
    var type: Int = PARTIAL_TYPE_UNKNOWN,
    val isGpsPartial: Boolean = false,
    var isDebugPartial: Boolean = false,

    val batteryLevel: Int? = null,

    // Points
    var nationalPoints: Int = 0,
    var initiativePoints: Int = 0,
    var urban: Boolean = false,

    // Log data
    val rawData_wheel: Long? = null,
    val rawData_ts: Int? = null,
    var extra: String? = null,

    // Backup data to restore service in case of sudden crash
    var totalGyroDistanceInKm: Double = 0.0,
    var totalGpsOnlyDistanceInKm: Double = 0.0,
    var totalGpsCacheDistanceInKm: Double = 0.0,
    var sessionElapsedTimeInSec: Int = 0,

    ) {
    companion object {
        val PARTIAL_TYPE_UNKNOWN = 0
        val PARTIAL_TYPE_START = 1
        val PARTIAL_TYPE_END = 2
        val PARTIAL_TYPE_IN_PROGRESS = 3
        val PARTIAL_TYPE_PAUSE = 4
        val PARTIAL_TYPE_RESUME = 5
        val PARTIAL_TYPE_SESSION = 6
        val PARTIAL_TYPE_SYSTEM = 7
        val PARTIAL_TYPE_SERVICE = 8
        val PARTIAL_TYPE_BLE = 9
        val PARTIAL_TYPE_GPS = 10
        val PARTIAL_TYPE_OTHER = 11

        fun getEmpty(sessionId: String) = PartialSessionDataEntity(
            System.currentTimeMillis(),
            sessionId = sessionId,
            altitude = 0.0,
            latitude = 0.0,
            longitude = 0.0,
        )
    }
    // Total distance should be a sum of both gps and gyro distance.
    // If gyro come back online during the session, its data while offline will be subtracted from
    // gps distance and counted only one time
    fun getTotalDistance() = gyroDistance + gpsDistance
    fun getReadableElapsedTime() = "%02d:%02d:%02d".format(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMillis),
        TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTimeInMillis)),
        TimeUnit.MILLISECONDS.toSeconds(elapsedTimeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTimeInMillis)));

    fun getAvgSpeedReadable() = "%.2f".format(averageSpeed)
    fun getTotalDistanceReadable() = "%.2f".format(getTotalDistance())
    fun getSpeedReadable() = "%.2f".format(speed)


}