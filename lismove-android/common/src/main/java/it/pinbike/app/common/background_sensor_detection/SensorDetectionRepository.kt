package it.lismove.app.common.background_sensor_detection

interface SensorDetectionRepository {
    /**
     * Returns timestamp of last sent notification/last session started
     * when a sensor was detected nearby.
     */
    suspend fun getLastSensorDetectionTs(): Long
    suspend fun setLastSensorDetectionTs(ts: Long)
}