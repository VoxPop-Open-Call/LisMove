package net.nextome.lismove_sdk

import net.nextome.lismove_sdk.models.LisMoveDevice

interface ConnectedDeviceCallback {
    fun onConnected(device: LisMoveDevice)
    fun onError(error: String)
}