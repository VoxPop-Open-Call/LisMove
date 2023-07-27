package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DebugLogEntity (
    @PrimaryKey
    val timestamp: Long,
    val content: String,
    val tag: String,
)

enum class DebugLogEntityTag { BLE, SESSION }