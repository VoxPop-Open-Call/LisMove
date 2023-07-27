package net.nextome.lismove_sdk.models

data class LisMoveSensorHistoryElement(
    val startLap: Int?,
    val stopLap: Int?,
    val startUtc: Long?,
    val stopUtc: Long?,
    val distance: Float? = null,
)