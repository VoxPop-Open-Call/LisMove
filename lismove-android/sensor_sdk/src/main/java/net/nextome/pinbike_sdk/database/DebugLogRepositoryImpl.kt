package net.nextome.lismove_sdk.database

import it.lismove.app.common.DateTimeUtils
import it.lismove.app.room.dao.DebugLogDao
import it.lismove.app.room.entity.DebugLogEntity
import it.lismove.app.room.entity.DebugLogEntityTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class DebugLogRepositoryImpl(
    private val dao: DebugLogDao,
): DebugLogRepository {
    override fun addSessionLogAsync(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            addDebugLogEntry(DebugLogEntity(System.currentTimeMillis(), message,
                DebugLogEntityTag.SESSION.name))
        }
    }

    override fun addBleLogAsync(message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            addDebugLogEntry(DebugLogEntity(System.currentTimeMillis(), message,
                DebugLogEntityTag.BLE.name))
        }
    }

    override suspend fun addDebugLogEntry(entry: DebugLogEntity) {
        withContext(Dispatchers.IO) {
            dao.addEntry(entry)
        }
    }

    override suspend fun getDebugLogEntries(): List<DebugLogEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext dao.getAll()
        }
    }

    override suspend fun getDebugLogEntriesAsString(): String {
        // Collect debug logs
        val logEntries = getDebugLogEntries()

        var stringifiedLog = ""
        val stringBuilder = StringBuilder()

        logEntries.forEach {
            stringBuilder.append("${it.timestamp}: ${it.content}\n")
        }

        return stringBuilder.toString()
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }

    override suspend fun deleteOldEntries(){
        withContext(Dispatchers.IO) {
            val todayAtMidnight = DateTimeUtils.getTodayAtMidnight()

            dao.deleteBefore(todayAtMidnight)
        }
    }
}