package it.lismove.app.android.maps.data



data class InitiativePolygon(
    val id: String,
    val initiative: String,
    val polygon: PolygonCoordinates,
    var isVisible: Boolean = true
)