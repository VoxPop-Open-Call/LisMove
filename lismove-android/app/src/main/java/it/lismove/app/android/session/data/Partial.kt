package it.lismove.app.android.session.data

class Partial (
    val timestamp: Long,
    var altitude: Double?,
    var latitude: Double?,
    var longitude: Double?,
    var type: Int,
    val deltaRevs: Long?,
    val urban: Boolean = true,
    val sensorDistance: Double?,
    val valid: Boolean? = null
)