package it.lismove.app.common

import android.Manifest
import android.os.Build

object LisMovePermissionsUtils {

    fun getGenericPermissions(): Array<String> {
        val commonPermissions = arrayOf(
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
        )

        val moreThanSPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
        )

        val lessThanSPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )

        val permissionsToAsk = arrayListOf<String>()
        permissionsToAsk.addAll(commonPermissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToAsk.addAll(moreThanSPermissions)
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            permissionsToAsk.addAll(lessThanSPermissions)
        }


        return permissionsToAsk.toTypedArray()
    }

    /**
     * Background Permission is separate from others becasuse it must be asked
     * ** AFTER ** location permission
     */
    fun getBackgroundLocationPermission() = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    fun hasToAskBackgroundLocationPermission() = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
}