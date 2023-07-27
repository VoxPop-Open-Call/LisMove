package net.nextome.lismove_sdk.sessionPoints.useCase

import it.lismove.app.room.dao.EnrollmentDao
import it.lismove.app.room.dao.OrganizationSessionPointDao
import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.sessionPoints.data.SessionPoints


class PointsManagerUseCaseImpl(
    val enrollmentDao: EnrollmentDao,
    val pointDao: OrganizationSessionPointDao,
    val sessionSdkRepository: SessionSdkRepository
): PointsManagerUseCase{

    override suspend fun getActiveInitiative(
        userId: String,
        date: Long
    ): List<EnrollmentWithOrganizationAndSettings> {
        return enrollmentDao.getActiveEnrollmentsWithOrganizationAndSettingsForUser(userId, date)
    }

    override suspend fun savePoints(points: List<OrganizationSessionPointEntity>) {
        pointDao.addOrUpdatePoints(points)
    }

    override suspend fun getSession(sessionId: String): SessionDataEntity?{
        return  sessionSdkRepository.getSessionById(sessionId)
    }

    override suspend fun getInitiativePoints(sessionId: String): List<OrganizationSessionPointEntity> {
        return pointDao.getSessionPoints(sessionId)
    }

    override suspend fun getSessionTotalDistance(sessionId: String): Double{
        return sessionSdkRepository.getLastSessionPartial(sessionId)?.getTotalDistance() ?: 0.0
    }

    override suspend fun getSessionNationalPoints(sessionId: String): Int{
        return sessionSdkRepository.getLastSessionPartial(sessionId)?.nationalPoints ?: 0
    }


}