package net.nextome.lismove_sdk.location

import net.nextome.lismove_sdk.models.LisMoveGpsPosition

data class LisMoveLocationDebug(
    val lastReceivedProviderGps: LisMoveGpsPosition? = null,
    val lastReceivedProviderPassive: LisMoveGpsPosition? = null,
    val lastReceivedProviderNetwork: LisMoveGpsPosition? = null,
)