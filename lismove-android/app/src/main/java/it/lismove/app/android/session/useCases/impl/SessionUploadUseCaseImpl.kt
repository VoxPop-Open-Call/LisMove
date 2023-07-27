package it.lismove.app.android.session.useCases.impl

import it.lismove.app.android.session.SessionHelper
import it.lismove.app.android.session.apiService.SessionRequest
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.parser.asOrganizationSessionPointEntity
import it.lismove.app.android.session.parser.asSessionDataEntity
import it.lismove.app.android.session.repository.ApplicationSessionRepository
import it.lismove.app.android.session.useCases.SessionUploadUseCase
import it.lismove.app.room.entity.SessionDataEntity
import net.nextome.lismove_sdk.database.SessionSdkRepository
import kotlin.Exception

class SessionUploadUseCaseImpl(
    val sessionSdkRepository: SessionSdkRepository,
    val sessionRepository: ApplicationSessionRepository
): SessionUploadUseCase {

    private val MIN_DISTANCE_IN_KM = 0.5

    override suspend fun uploadSession(userId: String, sessionId: String): Session {

        val sessionRequest = getSessionRequestIfValidSession(sessionId, userId)
        if(sessionRequest != null){
            try {
                val session = sessionRepository.createSession(sessionRequest)

                val sessionEntity = session.asSessionDataEntity()
                sessionSdkRepository.deleteSessionData(sessionId)
                sessionSdkRepository.addSessionOrReplace(sessionEntity)

                val sessionPointsEntity = session.sessionPoints.map { it.asOrganizationSessionPointEntity() }
                sessionSdkRepository.addPoints(sessionEntity.id, sessionPointsEntity)

                return session

            } catch (e: Exception) {
                val cachedSessionUpdated = sessionSdkRepository.getSessionById(sessionId)!!.copy(
                    status = SessionDataEntity.SESSION_STATUS_UPLOAD_FAILED,
                    proposedStatus = SessionDataEntity.SESSION_STATUS_UPLOAD_FAILED
                )
                sessionSdkRepository.updateSession(cachedSessionUpdated)
                throw e
            }
        } else {
            sessionSdkRepository.deleteSessionData(sessionId)
            throw SessionNotValidException()
        }
    }



    private suspend fun getSessionRequestIfValidSession(sessionId: String, userId: String): SessionRequest? {
        val session = sessionSdkRepository.getSessionById(sessionId)
        val partials = sessionSdkRepository.getPartials(sessionId) ?: listOf()
        val points = sessionSdkRepository.getPointsForSession(sessionId) ?: listOf()
        val distance = partials.lastOrNull { !it.isDebugPartial }?.getTotalDistance() ?: 0.0

        if (session != null && distance >= MIN_DISTANCE_IN_KM) {
            return SessionHelper.buildSessionRequest(userId, session, partials, points)
        } else {
            return null
        }
    }
}

data class SessionNotValidException(override val message: String = "Sessione troppo breve" ): Exception()