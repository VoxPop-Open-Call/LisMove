package net.nextome.lismove_sdk.database

import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.QueryData.OrganizationWithSessionPoint
import it.lismove.app.room.entity.SessionDataEntity
import kotlinx.coroutines.flow.Flow

interface SessionSdkRepository {
    suspend fun addPartial(entity: PartialSessionDataEntity)

    /** This method checks if the environment is dev/beta and only then
     * adds a debug partial
      */
    suspend fun addDebugPartial(sessionId: String, partialType: Int, partialExtra: String)

    suspend fun getPartials(sessionId: String): List<PartialSessionDataEntity>?
    fun getPartialsObservable(sessionId: String): Flow<List<PartialSessionDataEntity>>

    suspend fun addSessionOrIgnore(entity: SessionDataEntity)
    suspend fun addSessionOrReplace(entity: SessionDataEntity)

    suspend fun updateSession(entity: SessionDataEntity)
    suspend fun deleteSessionData(id: String)
    suspend fun updateStatus(id: String, status: Int)
    suspend fun updateProposedStatus(id: String, proposedStatus: Int?)

    suspend fun getSessionById(sessionId: String): SessionDataEntity?
    fun getSessionByIdObservable(sessionId: String): Flow<SessionDataEntity?>
    suspend fun getPointsForSession(sessionId: String): List<OrganizationSessionPointEntity>?
    suspend fun getPointsAndOrganizationForSession(sessionId: String): List<OrganizationWithSessionPoint>?

    suspend fun addPoints(sessionId: String, points: List<OrganizationSessionPointEntity>)
    suspend fun getActiveSession(): SessionDataEntity?
    fun getActiveSessionObservable(): Flow<SessionDataEntity?>
    suspend fun getLastSessionPartial(sessionId: String): PartialSessionDataEntity?
}