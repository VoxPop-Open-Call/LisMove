package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity
data class SessionDataEntity(
    @PrimaryKey val id: String,
    val status: Int = 0,
    var proposedStatus: Int? = null,

    var userId: String,
    var firmwareVersion: String? = null,
    var hubCoefficient: Double? = null,

    /* Populated by SDK */
    var startBattery: Int? = null,
    val endBattery: Int? = null,

    val phoneStartBattery: Int? = null,
    val phoneEndBattery: Int? = null,

    val startTime: Long? = null,
    val endTime: Long? = null,

    val gyroDistance: Double? = null,
    // Sum of distance in GPS Mode
    val gpsOnlyDistance: Double? = null,

    val totalKm: Double? = null,
    val polyline: String? = null,
    val description: String? = null, //ok

    /* Populated by SERVER */
    val type: Int? = null, //ok
    val nationalPoints: Int? = null, //ok
    val nationalKm: Double? = null,
    val valid: Boolean? = null, //ok

    val euro: Double? = null, //ok
    val certificated: Boolean = false, //ok
    val homeWorkPath: Boolean? = null, //ok
    val co2: Double? = null,
    val gmapsDistance: Double? = 0.0,
    val statusDescription: Int? = null,
    val duration: Long = 0L,
    var verificationRequired: Boolean? = null,
    val verificationRequiredNote: String? = null
) {
    companion object {
        const val SESSION_STATUS_CREATED = 0
        const val SESSION_STATUS_RUNNING = 1
        const val SESSION_STATUS_PAUSE_INACTIVITY = 2
        const val SESSION_STATUS_PAUSE_BY_USER = 3
        const val SESSION_STATUS_STOPPED = 4
        const val SESSION_STATUS_UPLOADED = 5
        const val SESSION_STATUS_UPLOAD_FAILED = 6

        val SESSION_NOT_UPLOADED_STATES = listOf(
            SESSION_STATUS_UPLOADED,
            SESSION_STATUS_UPLOAD_FAILED,
            SESSION_STATUS_STOPPED
        )
    }

    fun isCreated() = status == SESSION_STATUS_CREATED
    fun isRunning() = status == SESSION_STATUS_RUNNING
    fun isPaused() = (status == SESSION_STATUS_PAUSE_BY_USER) || (status == SESSION_STATUS_PAUSE_INACTIVITY)
    fun isStopped() = status == SESSION_STATUS_STOPPED
    fun isInProgress() = ( isRunning() || isPaused())
    fun isUploaded() = status == SESSION_STATUS_UPLOADED
    fun isUploadFailed() = status == SESSION_STATUS_UPLOAD_FAILED
}