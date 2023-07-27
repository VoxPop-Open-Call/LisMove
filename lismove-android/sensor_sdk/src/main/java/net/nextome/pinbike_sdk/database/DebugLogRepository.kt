package net.nextome.lismove_sdk.database

import it.lismove.app.room.entity.DebugLogEntity

interface DebugLogRepository {
    fun addSessionLogAsync(message: String)
    fun addBleLogAsync(message: String)

    suspend fun addDebugLogEntry(entry: DebugLogEntity)
    suspend fun getDebugLogEntries(): List<DebugLogEntity>
    suspend fun getDebugLogEntriesAsString(): String
    suspend fun deleteAll()
    suspend fun deleteOldEntries()
}