package net.nextome.lismove_sdk.models


/**
 * Models a status update from the LisMove Service Worker.
 * BLE statuses are used only to inform the UI about the connection progress.
 *
 * Fatal errors cause the WorkManager to stop the session and shut down the service.
 *
 * You can safely show fatal error motivation to the user using errorMessage
 */
data class LisMoveServiceState(
    val status: LisMoveServiceStatus,
    val errorMessage: String = ""
)

enum class LisMoveServiceStatus { WORKING, GENERIC_ERROR, FATAL_ERROR, }