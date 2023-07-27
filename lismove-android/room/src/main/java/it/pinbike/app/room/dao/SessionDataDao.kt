package it.lismove.app.room.dao

import androidx.room.*
import it.lismove.app.room.entity.QueryData.SessionWithPoints
import it.lismove.app.room.entity.SessionDataEntity
import it.lismove.app.room.entity.SessionProposedStatusUpdateEntity
import it.lismove.app.room.entity.SessionStateUpdateEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface SessionDataDao {

    @Query("DELETE FROM sessiondataentity WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("SELECT * FROM sessiondataentity WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionDataEntity?

    @Query("SELECT * FROM sessiondataentity WHERE id = :sessionId")
    fun getSessionByIdObservable(sessionId: String): Flow<List<SessionDataEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addOrIgnore(entity: SessionDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrReplace(entity: SessionDataEntity)

    @Update(entity = SessionDataEntity::class)
    suspend fun updateSessionStatus(sessionStateUpdate: SessionStateUpdateEntity)

    @Update(entity = SessionDataEntity::class)
    suspend fun updateProposedSessionStatus(proposedStatusUpdate: SessionProposedStatusUpdateEntity)

    @Query("SELECT * FROM sessiondataentity WHERE  status < 5 order by startTime desc")
    suspend fun getActiveSession(): SessionDataEntity?

    @Query("SELECT * FROM sessiondataentity WHERE id = :sessionId")
    @Transaction
    suspend fun getSessionWithPoints(sessionId: String): SessionWithPoints

    @Query("SELECT * FROM sessiondataentity WHERE  status < 5")
    fun getActiveSessionObservable(): Flow<SessionDataEntity?>

    @Query("SELECT * FROM sessiondataentity WHERE  status in  (5, 6, 4)   AND userId == :userId AND startTime >= :startDate AND startTime <= :endDate order by startTime desc")
    @Transaction
    suspend fun getSessionListWithPoints(userId: String, startDate: Long, endDate: Long): List<SessionWithPoints>

    @Query("SELECT * FROM sessiondataentity WHERE status in (6, 4)AND userId == :userId ")
    @Transaction
    suspend fun getNotUploadedSessionWithPoints(userId: String): List<SessionWithPoints>
}