package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.PartialSessionDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartialSessionDataDao {
    @Query("SELECT * FROM partialsessiondataentity WHERE sessionId = :sessionId")
    suspend fun getPartialsBySession(sessionId: String): List<PartialSessionDataEntity>?

    @Query("SELECT * FROM partialsessiondataentity WHERE sessionId = :sessionId")
    fun getPartialsBySessionObservable(sessionId: String): Flow<List<PartialSessionDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: PartialSessionDataEntity)

    @Query("DELETE FROM partialsessiondataentity WHERE sessionId = :sessionId")
    suspend fun deletePartialsBySessionId(sessionId: String)

    @Query("SELECT * FROM partialsessiondataentity WHERE sessionId = :sessionId AND isDebugPartial = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSessionPartial(sessionId: String): PartialSessionDataEntity?
}