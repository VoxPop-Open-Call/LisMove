package net.nextome.lismove_sdk.database

import it.lismove.app.room.dao.OrganizationSessionPointDao
import it.lismove.app.room.dao.PartialSessionDataDao
import it.lismove.app.room.dao.SessionDataDao
import it.lismove.app.room.entity.*
import it.lismove.app.room.entity.QueryData.OrganizationWithSessionPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionSdkRepositoryImpl(
    private val sessionDataDao: SessionDataDao,
    private val partialSessionDataDao: PartialSessionDataDao,
    private val organizationSessionPointDao: OrganizationSessionPointDao,
): SessionSdkRepository {
    override suspend fun addPartial(entity: PartialSessionDataEntity) {
        partialSessionDataDao.add(entity)
    }

    override suspend fun addDebugPartial(sessionId: String, partialType: Int, partialExtra: String) {
//        if (BuildConfig.DEBUG) {
            val entity = PartialSessionDataEntity.getEmpty(sessionId).apply {
                type = partialType
                extra = partialExtra
                isDebugPartial = true
            }

            partialSessionDataDao.add(entity)
//        }
    }

    override suspend fun getPartials(sessionId: String): List<PartialSessionDataEntity>? {
        return partialSessionDataDao.getPartialsBySession(sessionId)
    }

    override fun getPartialsObservable(sessionId: String): Flow<List<PartialSessionDataEntity>> {
        return partialSessionDataDao.getPartialsBySessionObservable(sessionId)
    }

    override suspend fun addSessionOrReplace(entity: SessionDataEntity) {
        return sessionDataDao.addOrReplace(entity)
    }

    override suspend fun addSessionOrIgnore(entity: SessionDataEntity) {
        return sessionDataDao.addOrIgnore(entity)
    }

    override suspend fun updateSession(entity: SessionDataEntity) {
        return sessionDataDao.addOrReplace(entity)
    }

    override suspend fun deleteSessionData(id: String) {
        sessionDataDao.deleteSessionById(id)
        partialSessionDataDao.deletePartialsBySessionId(id)
        organizationSessionPointDao.deleteAll(id)
    }

    override suspend fun updateStatus(id: String, status: Int) {
        return sessionDataDao.updateSessionStatus(SessionStateUpdateEntity(id, status, null))
    }

    override suspend fun updateProposedStatus(id: String, proposedStatus: Int?) {
        return sessionDataDao.updateProposedSessionStatus(SessionProposedStatusUpdateEntity(id, proposedStatus))
    }

    override suspend fun getSessionById(sessionId: String): SessionDataEntity? {
        return sessionDataDao.getSessionById(sessionId)
    }

    override fun getSessionByIdObservable(sessionId: String): Flow<SessionDataEntity?> {
        return sessionDataDao.getSessionByIdObservable(sessionId).map { it.firstOrNull() }
    }

    override suspend fun getPointsForSession(sessionId: String): List<OrganizationSessionPointEntity> {
        return organizationSessionPointDao.getSessionPoints(sessionId)
    }

    override suspend fun getPointsAndOrganizationForSession(sessionId: String): List<OrganizationWithSessionPoint> {
        return organizationSessionPointDao.getSessionPointsWithOrganization(sessionId)
    }

    override suspend fun addPoints(
        sessionId: String,
        points: List<OrganizationSessionPointEntity>
    ) {
       return organizationSessionPointDao.addOrUpdatePoints(points)
    }

    override suspend fun getActiveSession(): SessionDataEntity? {
        return sessionDataDao.getActiveSession()
    }

    override fun getActiveSessionObservable(): Flow<SessionDataEntity?> {
        return sessionDataDao.getActiveSessionObservable()
    }

    override suspend fun getLastSessionPartial(sessionId: String): PartialSessionDataEntity? {
        return partialSessionDataDao.getLastSessionPartial(sessionId)
    }
}