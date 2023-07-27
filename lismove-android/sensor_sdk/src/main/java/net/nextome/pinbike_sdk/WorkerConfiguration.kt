package net.nextome.lismove_sdk

import it.lismove.app.room.entity.SensorEntity
import net.nextome.lismove_sdk.models.LisMoveDevice

data class WorkerConfiguration (
    val device: SensorEntity? = null,
    val sessionId: String,
    val userId: String,

    // is start or resume from pause
    val isResume: Boolean = false,
)