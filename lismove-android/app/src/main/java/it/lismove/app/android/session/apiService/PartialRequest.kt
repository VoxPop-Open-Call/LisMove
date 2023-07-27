package it.lismove.app.android.session.apiService

data class PartialRequest (
    val timestamp: Long,
    var altitude: Double?,
    var latitude: Double?,
    var longitude: Double?,
    val deltaRevs: Long?,
    val urban: Boolean,
    var type: Int = 0,
    val sensorDistance: Double?,
    val rawData_wheel: Long?,
    val rawData_ts: Int?,
    val extra: String?,
)