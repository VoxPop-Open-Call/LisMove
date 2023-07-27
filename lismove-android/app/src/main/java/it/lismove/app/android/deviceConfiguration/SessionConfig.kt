package it.lismove.app.android.deviceConfiguration

import net.nextome.lismove_sdk.models.LisMoveDevice

data class SessionConfig(
    val wheelDiameterInMm: Double,
    val lismoveDevice: LisMoveDevice
)