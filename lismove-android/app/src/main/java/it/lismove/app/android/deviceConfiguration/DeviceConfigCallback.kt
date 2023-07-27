package it.lismove.app.android.deviceConfiguration

interface DeviceConfigCallback {
    fun onWheelDimenConfirmed(valueSelected: String)
    fun onBikeTypeConfirmed(valueSelected: String)
}