package net.nextome.lismove_sdk.models

data class LisMoveGpsPosition(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val elapsedTimeNanos: Long,
)
