package it.lismove.app.android.session.apiService

import it.lismove.app.android.session.data.SessionPoint

data class SessionRequest (
    val description: String,
    val startBattery: Int?,
    val endBattery: Int?,
    val startTime: Long,
    val endTime: Long,
    val gyroDistance: Double,
    val nationalPoints: Int,
    val partials: List<PartialRequest>,
    val phoneStartBattery: Int?,
    val phoneEndBattery: Int?,
    val sessionPoints: List<SessionPoint>,
    val firmware: String?,
    val hubCoefficient: Double?,
    val uid: String,
    val gpsOnlyDistance: Double?,
    val duration: Long,
)