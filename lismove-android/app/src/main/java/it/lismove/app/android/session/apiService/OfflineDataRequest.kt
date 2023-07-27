package it.lismove.app.android.session.apiService

import net.nextome.lismove_sdk.models.LisMoveSensorHistoryElement

data class OfflineDataRequest(
    val distance: Float?,
    val endRevs: Int?,
    val endTime: Long?,
    val sensor: String?,
    val startRevs: Int?,
    val startTime: Long?,
    val user: String?, )

fun LisMoveSensorHistoryElement.asOfflineDataRequest(userId: String?, sensorName: String?) =
    OfflineDataRequest(distance, stopLap, stopUtc, sensorName, startLap, startUtc, userId)