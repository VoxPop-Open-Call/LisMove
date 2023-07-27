package net.nextome.lismove_sdk.sessionPoints.useCase

import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.sessionPoints.data.SessionPoints

interface PointsManagerUseCase{
    suspend fun getActiveInitiative(userId: String, date: Long): List<EnrollmentWithOrganizationAndSettings>
    suspend fun savePoints(points: List<OrganizationSessionPointEntity>)

    suspend fun getSession(sessionId: String): SessionDataEntity?
    suspend fun getInitiativePoints(sessionId: String): List<OrganizationSessionPointEntity>
    suspend fun getSessionTotalDistance(sessionId: String): Double
    suspend fun getSessionNationalPoints(sessionId: String): Int
}