package net.nextome.lismove_sdk.utils

import android.location.Location
import android.util.Log
import com.google.gson.Gson
import it.lismove.app.room.entity.PartialSessionDataEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.nextome.lismove_sdk.models.CadencePrimitive
import net.nextome.lismove_sdk.models.LisMoveGpsPosition
import timber.log.Timber


class SensorDataManager(val wheelCircunference: Double, val sessionId: String,val hubCoefficient: Double) {
    private val MAX_SPEED_LIMIT_IN_KMH = 80.0 // max speed
    private val MIN_SPEED_LIMIT_IN_KMH = 5.0 // min speed for gps
    private val GPS_PAUSE_INIT_TIME = 3

    private val UINT16_MAX: Long = 65535
    private val UINT32_MAX: Long = 4294967295

    private var lastMeasurementSpeed = Array<Double>(4) { -1.0 }
    private var lastMeasurementSpeedIndex = 0
    private var speedInKmH: Double = 0.0

    var totalGyroDistanceInKm: Double = 0.0
    var totalGpsOnlyDistanceInKm: Double = 0.0
    // Cache for gps distance
    // If switch to gyro, this value is added in gyroDistance
    // If session pauses/closes in GPS, this value is added in gpsOnlyDistance
    var totalGpsCacheDistanceInKm: Double = 0.0


    var lastSensorBattery = -1

    var sessionElapsedTimeInSec = 0

    var previousCadenceEntity: CadencePrimitive? = null
    var previousGpsEntity: LisMoveGpsPosition? = null

    // private var lastGpsSessionDistanceInKm: Double = 0.0
    private var lastGpsSessionTimeSeconds: Long? = null


    var isGpsOnlyMode = true

    /* Pause */
    var isPauseForced = false
    var isSessionActive = false
    private var lastPauseReason = ""

    // Don't pause again after a forced resume
    private var hasResumedShortly = false

    // GPS pause
    var pauseOnGpsInactivityEnabled = false
    var currentPauseOnGpsTimeoutInSeconds = 30

    var lastSavedGyroDistance = 0.0

    var errorFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    fun forcePause() { isPauseForced = true }
    fun resumeFromPause() {
        isPauseForced = false
        isSessionActive = false
        hasResumedShortly = true
    }

    /**
     * If the sensor doesn't move for 5 seconds,
     * the session is marked as inactive (pause)
     */
    fun isSessionActiveObservable(secondsToPause: Int = 5) = flow<Boolean> {
        val speedMeasurement = Array<Double>(secondsToPause) { -1.0 }
        var i = 0

        // when gps pause is enabled, wait at least 3 seconds of speed=0 before pausing
        var gpsPauseInitTime = GPS_PAUSE_INIT_TIME

        while (true) {
            if (!isPauseForced) {
                if (isGpsOnlyMode) {
                    // GPS MODE
                    if (pauseOnGpsInactivityEnabled) {
                        // check if ignore timeout has elapsed
                        if (currentPauseOnGpsTimeoutInSeconds <= 1) {
                            pauseOnGpsInactivityEnabled = false
                        }

                        speedMeasurement[i] = speedInKmH
                        i = (i + 1) % secondsToPause

                        // check if stopped for secondsToPause secs
                        if (speedMeasurement.filter { it == 0.0 }.size == speedMeasurement.size) {

                            if (gpsPauseInitTime > 0) {
                                with("Wait gps pause init time ${gpsPauseInitTime}"){
                                    lastPauseReason = this
                                    Timber.i(this)
                                }

                                gpsPauseInitTime -= 1
                                sessionElapsedTimeInSec += 1
                                isSessionActive = true
                                Timber.e("true 1")
                                emit(true)
                            } else {
                                with("PAUSED on GPS since sensor was disconnected recently") {
                                    lastPauseReason = this
                                    Timber.i(this)
                                }

                                gpsPauseInitTime = GPS_PAUSE_INIT_TIME
                                forcePause()

                                pauseOnGpsInactivityEnabled = false
                                isSessionActive = false

                                Timber.e("false 2")
                                emit(false)
                            }
                        } else if (speedMeasurement.filter { it != 0.0 }.size > 2) {
                            with("NOT PAUSED on GPS even if sensor was disconnected recently because speed is not 0") {
                                Timber.i(this)
                            }

                            currentPauseOnGpsTimeoutInSeconds -= 1
                            sessionElapsedTimeInSec += 1
                            isSessionActive = true
                            Timber.e("true 3")
                            emit(true)
                        }
                    } else {
                        with("can't pause since we're on gps mode") {
                            Timber.i(this)
                        }

                        // gps can't be automatically paused
                        sessionElapsedTimeInSec += 1

                        // hasResumedShortly = false
                        isSessionActive = true

                        Timber.e("true 4")
                        emit(true)
                    }
                } else {
                    // NOT IN GPS MODE
                    speedMeasurement[i] = speedInKmH
                    i = (i + 1) % secondsToPause

                    // check if stopped for secondsToPause secs
                    if (speedMeasurement.filter { it == 0.0 }.size == speedMeasurement.size) {
                        if (!hasResumedShortly) {
                            with ("PAUSED for sensor inactivity") {
                                lastPauseReason = this
                                Timber.i(this)
                            }

                            isSessionActive = false

                            Timber.e("false 5")
                            emit(false)
                        } else {
                            // Resume was forced, even if velocity is 0, resume session
                            with ("NOT PAUSED since has resumed shortly") {
                                Timber.i(this)
                            }

                            sessionElapsedTimeInSec += 1
                            isSessionActive = true

                            Timber.e("true 6")
                            emit(true)
                        }
                        // Automatically resume session only after two partials != 0
                    } else if (speedMeasurement.filter { it != 0.0}.size > 1) {
                        with ("NOT PAUSED since speed is not 0") {
                            Timber.i(this)
                        }

                        sessionElapsedTimeInSec += 1
                        isSessionActive = true

                        Timber.e("true 7")
                        emit(true)

                        hasResumedShortly = false
                    }
                }

                // collect speeds each second
            } else {
                with ("PAUSED because pause is forced by user") {
                    lastPauseReason = this
                    Timber.i(this)
                }

                isSessionActive = false

                Timber.e("false 8")
                emit(false)
            }

            delay(1000)
        }
    }.distinctUntilChanged()

    /**
     * Enable automatic pause even on GPS mode if speed is 0.
     * After timeout seconds are elapsed, even if speed is 0,
     * gps pause will be ignored again.
     *
     * This happens when the user disconnects a sensor during a session:
     * if gps is computing new positions, don't pause
     * if gps is on the same position (the user is arrived at home), pause the session
     * (he may have left the bike with the sensor and he's not running anymore).
     * If gps is still computing new positions, ignore GPS automatic pauses after n seconds timeout
     */
    fun setPauseOnGpsInactivityEnabled(timeoutInSeconds: Int) {
        currentPauseOnGpsTimeoutInSeconds = timeoutInSeconds
        pauseOnGpsInactivityEnabled = true
    }


    fun getPartial(currentGpsEntity: LisMoveGpsPosition?): PartialSessionDataEntity? {
        Log.e("gpsData", "gpsCache: $totalGpsCacheDistanceInKm")
        Log.e("gpsData", "gpsOnlyDistance: $totalGpsOnlyDistanceInKm")
        Log.e("gpsData", "gyroDistance: $totalGyroDistanceInKm")

        if (!isSessionActive) { return null }
        if (currentGpsEntity == null) { return null }

        if (previousGpsEntity == null) {
            previousGpsEntity = currentGpsEntity
            lastGpsSessionTimeSeconds = System.currentTimeMillis() / 1000
            return null
        }

        val currentGpsSessionTimeSeconds = System.currentTimeMillis() / 1000

        val startLocation = Location("").apply {
            latitude = previousGpsEntity!!.latitude
            longitude = previousGpsEntity!!.longitude
        }

        val currentLocation = Location("").apply {
            latitude = currentGpsEntity.latitude
            longitude = currentGpsEntity.longitude
        }

        val calculatedDistanceInKm = (startLocation?.distanceTo(currentLocation) ?: 0F) / 1000

        val computedSpeedInKmH = if (lastGpsSessionTimeSeconds != null) {
            ((calculatedDistanceInKm / (currentGpsSessionTimeSeconds - (lastGpsSessionTimeSeconds ?: 0))) * 3600)
        } else { 0.0 }

        // update location, even if outlier (will be cleaned with correct position after a while)
        previousGpsEntity = currentGpsEntity

        if (computedSpeedInKmH.toDouble() >= MAX_SPEED_LIMIT_IN_KMH) {
            val message = "GPS Speed exceeded max speed (got $computedSpeedInKmH, max is $MAX_SPEED_LIMIT_IN_KMH km/h)"
            Log.e("GpsData", message)
            errorFlow.value = message

            // Discard partial and don't update average speed and total km
            return getEmptyPartial()
        }

        if (computedSpeedInKmH.toDouble() <= MIN_SPEED_LIMIT_IN_KMH && computedSpeedInKmH.toDouble() != 0.0) {
            val message = "GPS Speed below limit (got $computedSpeedInKmH, min is $MIN_SPEED_LIMIT_IN_KMH km/h"
            Log.e("GpsData", message)
            errorFlow.value = message

            return getEmptyPartial()
        }

        totalGpsCacheDistanceInKm += calculatedDistanceInKm

        val avgSpeed = getAverageSpeed()

        // don't update time if partial was discarded. This is because if not,
        // the intervals would be always of 1 secs (and gps can jump a lot at a more slower rate)

        if (computedSpeedInKmH != 0.0) {
            lastGpsSessionTimeSeconds = currentGpsSessionTimeSeconds
        }

        return PartialSessionDataEntity(
            System.currentTimeMillis(),
            sessionId = sessionId,
            altitude = currentGpsEntity.altitude,
            latitude = currentGpsEntity.latitude,
            longitude = currentGpsEntity.longitude,
            deltaRevs = null,
            wheelTime = 0,
            gyroDeltaDistance = null,
            speed = (computedSpeedInKmH?.toDouble()) ?: 0.0,
            gyroDistance = if (totalGyroDistanceInKm >= 0.0) totalGyroDistanceInKm else { 0.0 },
            gpsDistance = totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm,
            elapsedTimeInMillis = getSessionElapsedTimeInMillis(),
            averageSpeed = avgSpeed,
            batteryLevel = lastSensorBattery,
            isGpsPartial = true,
            extra = getGenericPartialExtraString()
        )
    }

    /**
     * Human readable reason of last pause.
     * May be forced by user, automatic pause or sensor disconnected pause
     */
    fun getLastPauseReason() = lastPauseReason


    data class PartialGenericExtra(
        val totalGyroDistanceInKm: Double?,
        val totalGpsOnlyDistanceInKm: Double?,
        val totalGpsCacheDistanceInKm: Double?,
    )

    private fun getGenericPartialExtraString(): String? {
        return Gson().toJson(PartialGenericExtra(totalGyroDistanceInKm, totalGpsOnlyDistanceInKm, totalGpsCacheDistanceInKm))
    }


    fun getPartial(currentSample: CadencePrimitive): PartialSessionDataEntity? {

        Log.e("gpsData", "gpsCache: $totalGpsCacheDistanceInKm")
        Log.e("gpsData", "gpsOnlyDistance: $totalGpsOnlyDistanceInKm")
        Log.e("gpsData", "gyroDistance: $totalGyroDistanceInKm")

        // if coming from GPS session, override GPS distance data with sensor one (except if started in gps)
        if (totalGpsCacheDistanceInKm != 0.0) {
            // Was started in GPS
            if (previousCadenceEntity == null) {
                totalGpsOnlyDistanceInKm = totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm
            } else {
                // should recover distance from gyro data
            }

            totalGpsCacheDistanceInKm = 0.0
            previousGpsEntity = null
        }

        if (currentSample.wheelRevs < 0) currentSample.wheelRevs = 0

        if (previousCadenceEntity == null) {

            // is first sample
            previousCadenceEntity = currentSample

            return getEmptyPartial()
        } else {
            var wheelTimeDiffInSeconds: Double = diffForSample(
                currentSample.wheelTime.toLong(),
                previousCadenceEntity!!.wheelTime.toLong(),
                UINT16_MAX
            ).toDouble()

            wheelTimeDiffInSeconds /= 1024 // Convert from fractional seconds (roughly ms) -> full seconds

            var wheelDiff: Double = diffForSample(
                currentSample.wheelRevs.toLong(),
                previousCadenceEntity!!.wheelRevs.toLong(),
                UINT32_MAX
            ).toDouble()

            // Wheel has exceeded limit
            if (wheelDiff<0) {
                wheelDiff = 0.0
                emptyCache()
            }

            if (wheelDiff > 50) {
                val message = "Found a big wheelDiff value ($wheelDiff). Current wheel=${currentSample.wheelRevs}, previous wheel=${previousCadenceEntity?.wheelRevs}"
                errorFlow.value = message
            }

            if (wheelDiff > 100) {
                // Limit wheel diff values
                val message = "Found a big wheelDiff value ($wheelDiff). Value was ignored. Current wheel=${currentSample.wheelRevs}, previous wheel=${previousCadenceEntity?.wheelRevs}"
                errorFlow.value = message
                wheelDiff = 0.0
            }

            var sampleDistanceInMeters: Double = wheelDiff * wheelCircunference / 1000 * hubCoefficient// distance in meters

            speedInKmH = if (wheelTimeDiffInSeconds == 0.0) {
                0.0
            } else {
                sampleDistanceInMeters / wheelTimeDiffInSeconds * 3.6
            } // km/h

            // update cumulative distance only if not in pause
            if (isSessionActive) {
                totalGyroDistanceInKm += (sampleDistanceInMeters / 1000)
            }

            if (totalGyroDistanceInKm < 0.0) { totalGyroDistanceInKm = 0.0 }

            lastSavedGyroDistance = totalGyroDistanceInKm
            // Set this as previous sample
            previousCadenceEntity = currentSample

            //Avg for speed
            lastMeasurementSpeed[lastMeasurementSpeedIndex] = speedInKmH
            lastMeasurementSpeedIndex = (lastMeasurementSpeedIndex + 1) % 4
            var lastSpeedSum = 0.0
            var lastSpeedCount = 0

            lastMeasurementSpeed.forEach { speed ->
                if (speed != -1.0) {
                    lastSpeedSum += speed
                    lastSpeedCount += 1
                }
            }

            val computedSpeed = if (lastSpeedCount != 0) {
                 lastSpeedSum / lastSpeedCount
            } else { 0.0 }

            if (computedSpeed >= MAX_SPEED_LIMIT_IN_KMH) {
                // ignore partial if exceeded max speed
                val message = "GYRO Speed exceeded max speed (got $computedSpeed, max is $MAX_SPEED_LIMIT_IN_KMH km/h)"
                errorFlow.value = message
                BugsnagUtils.reportIssue(Exception(message))
                return getEmptyPartial()
            }

            val avgSpeed = getAverageSpeed()


            return PartialSessionDataEntity(
                System.currentTimeMillis(),
                sessionId = sessionId,
                altitude = 0.0,
                latitude = 0.0,
                longitude = 0.0,
                deltaRevs = wheelDiff.toLong(),
                wheelTime = currentSample.wheelTime,
                speed = computedSpeed,
                gyroDistance = totalGyroDistanceInKm,
                gyroDeltaDistance = sampleDistanceInMeters / 1000,
                gpsDistance = totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm,
                elapsedTimeInMillis = getSessionElapsedTimeInMillis(),
                averageSpeed = avgSpeed,
                batteryLevel = lastSensorBattery,
                isGpsPartial = false,
                rawData_wheel = currentSample.wheelRevs,
                rawData_ts = currentSample.wheelTime,
                extra = getGenericPartialExtraString(),
            )
        }
    }

    fun getEmptyPartial(): PartialSessionDataEntity {
        return PartialSessionDataEntity(
            System.currentTimeMillis(),
            sessionId = sessionId,
            altitude = 0.0,
            latitude = 0.0,
            longitude = 0.0,
            deltaRevs = 0,
            wheelTime = 0,
            speed = 0.0,
            gyroDistance = if (totalGyroDistanceInKm >= 0.0) totalGyroDistanceInKm else { 0.0 },
            gpsDistance = (totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm),
            elapsedTimeInMillis = getSessionElapsedTimeInMillis(),
            averageSpeed = getAverageSpeed(),
            batteryLevel = lastSensorBattery,
            isGpsPartial = true,
            extra = getGenericPartialExtraString(),
        )
    }

    // Used after pause or stop
    fun emptyCache() {
        // Check if there's data in gps cache
        if (totalGpsCacheDistanceInKm != 0.0) {
            totalGpsOnlyDistanceInKm += totalGpsCacheDistanceInKm
            totalGpsCacheDistanceInKm = 0.0
        }

        previousCadenceEntity = null
        previousGpsEntity = null
    }

    /**
     * Returns false if it was impossible to recover session
     */
    fun recoverFromSessionPartials(activeSessionPartials: List<PartialSessionDataEntity>): Boolean {
        val lastRecoveredPartial = activeSessionPartials
            .filter { !it.isDebugPartial }
            .maxByOrNull { it.timestamp } ?: return false

        totalGyroDistanceInKm = lastRecoveredPartial.totalGyroDistanceInKm
        totalGpsOnlyDistanceInKm = lastRecoveredPartial.totalGpsOnlyDistanceInKm
        totalGpsCacheDistanceInKm = lastRecoveredPartial.totalGpsCacheDistanceInKm
        sessionElapsedTimeInSec = lastRecoveredPartial.sessionElapsedTimeInSec

        return true
    }

    private fun getAverageSpeed(): Double {
        val avgSpeed = if (sessionElapsedTimeInSec != 0) {
            getTotalDistance() / (sessionElapsedTimeInSec * 1000) * 1000000 * 3.6
        } else {
            0.0
        }
        return avgSpeed
    }

    private fun diffForSample(current: Long, previous: Long, max: Long): Long {
        val diff = if (current >= previous) {
            (current - previous)
        } else {
            ((max - previous) + current)
        }

        if (diff<0) return 0

        return diff
    }

    fun getSessionElapsedTimeInMillis() = sessionElapsedTimeInSec.toLong() * 1000L
    fun getTotalDistance() = (totalGpsOnlyDistanceInKm + totalGpsCacheDistanceInKm + totalGyroDistanceInKm)

    companion object {
        fun calculateDistanceInKm(startRevs: Int?, endRevs: Int?, circumference: Float?, hubCoefficient: Double): Float {
            if (startRevs == null || endRevs == null || circumference == null) { return 0F }
            return (endRevs - startRevs) * circumference / 1000 / 1000  * hubCoefficient.toFloat()// km
        }
    }
}