package it.lismove.app.android.session.ui.useCase

import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.data.SessionPoint
import it.lismove.app.android.session.parser.asSessionPoint
import it.lismove.app.android.session.repository.ApplicationSessionRepository
import net.nextome.lismove_sdk.database.DebugLogRepository
import net.nextome.lismove_sdk.database.SessionSdkRepository
class SessionDetailUseCaseImpl(
    private val organizationRepository: OrganizationRepository,
    private val repository: ApplicationSessionRepository,
    private val sessionSdkRepository: SessionSdkRepository,
    private val userRepository: UserRepository,
    private val debugLogRepository: DebugLogRepository,
): SessionDetailUseCase {
    override suspend fun getSession(id: String, uid: String): Session {
        val session = repository.getSession(id, uid)
        if(session.sessionPoints.isEmpty()){

            val activeInitiative = userRepository.getActiveInitiativesWithSettings(uid, session.startTime)
            session.sessionPoints = activeInitiative.map{
                SessionPoint(
                    0.0,
                    1.0,
                    it.organization.id,
                    0,
                    id,
                    0.0,
                    null,
                )
            }
        } else{

            session.sessionPoints.forEach { point ->
                val organization = organizationRepository.getOrganization(point.organizationId)
                val settings = organizationRepository.getSettings(point.organizationId)
                    point.hasRefundEnabled = settings.homeWorkRefund || settings.initiativeRefund
                    point.organizationName = organization.title
                }
         }
        return session
    }

    override suspend fun getSessionPoints(sessionId: String, userId: String): List<ListAlertData> {
        val points = sessionSdkRepository.getPointsAndOrganizationForSession(sessionId) ?: listOf()

        if(points.isEmpty()){
            val session = repository.getSession(sessionId, userId)
            val enrollmentsWithOrganization = userRepository.getActiveInitiatives(userId, session.startTime)
            return enrollmentsWithOrganization.map { ListAlertData(it.organization.id.toString(),  it.organization.title, "0") }
        }else{
            return points.map {
                ListAlertData(it.organization.id.toString(), it.organization.title, it.pointEntity.points.toString())
            }
        }
    }

    override suspend fun requestVerification(sessionId: String, userId: String, reason: String, types: List<Int>): Session {
        val result = repository.requestSessionVerification(sessionId, reason, userId, types)
        debugLogRepository.deleteAll()
        return result
    }

    override suspend fun requestPointVerification(sessionId: String, userId: String, reason: String): Session {
        val pointType = repository.getPointFeedbackFormId()
        return repository.requestSessionVerification(sessionId, reason, userId, listOf(pointType))
    }

    override suspend fun getSessionWithPartials(sessionId: String): Session {
        return repository.getSessionWithPartials(sessionId)
    }
}