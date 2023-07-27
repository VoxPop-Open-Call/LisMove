package net.nextome.lismove_sdk.location

import android.location.Location

enum class OutdoorLocationType{ GPS, NETWORK, PASSIVE }

data class OutdoorLocation(
    val location: Location?,
    val type: OutdoorLocationType
)