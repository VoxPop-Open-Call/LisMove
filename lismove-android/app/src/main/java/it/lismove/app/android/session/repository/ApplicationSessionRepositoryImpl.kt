package it.lismove.app.android.session.repository

import it.lismove.app.android.general.utils.toJson
import com.bugsnag.android.internal.DateUtils
import it.lismove.app.android.session.apiService.SessionApi
import it.lismove.app.android.session.apiService.SessionRequest
import it.lismove.app.android.session.apiService.SessionValidationRequest
import it.lismove.app.android.session.data.FeedBackFormOption
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.parser.asOrganizationSessionPointEntity
import it.lismove.app.android.session.parser.asSession
import it.lismove.app.android.session.parser.asSessionDataEntity
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.room.dao.OrganizationSessionPointDao
import kotlinx.coroutines.flow.Flow
import it.lismove.app.room.dao.SessionDataDao
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import org.joda.time.LocalDate
import net.nextome.lismove_sdk.database.DebugLogRepository
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.lang.StringBuilder

class ApplicationSessionRepositoryImpl(
    private val sessionApi: SessionApi,
    private val sessionDao: SessionDataDao,
    private val organizationSessionPointDao: OrganizationSessionPointDao,
    private val debugLogRepository: DebugLogRepository,
): ApplicationSessionRepository {

    override suspend fun createSession(session: SessionRequest): Session {
       return sessionApi.createSession(session)
    }

    override suspend fun getSession(sessionId: String, userId: String): Session {
        try {
            val session = sessionApi.getSession(sessionId)
            addOrUpdateSessionOnRoom(session, userId)
            return session
        } catch (exception: Exception) {
            return sessionDao.getSessionWithPoints(sessionId).asSession()
        }
    }

    override suspend fun getSessionWithPartials(sessionId: String): Session {
        return sessionApi.getSession(sessionId, withPartials = true)
    }

    override suspend fun getSessionList(userId: String, start: Date, end: Date): List<Session> {
        try {
            val startDateString = DateTimeUtils.getFilterString(start)
            val endDateString = DateTimeUtils.getFilterString(end)
            val sessions = sessionApi.getUserSessions(userId, startDateString, endDateString)
            sessions.forEach {
               addOrUpdateSessionOnRoom(it, userId)
            }
        }catch (exception: IOException){
            exception.printStackTrace()
        }
        val endTime = DateTime().withDate(LocalDate(end)).plusDays(1).millis
        Timber.d("getSessionList ${start.time} - ${end}")

        return sessionDao.getSessionListWithPoints(userId, start.time, endTime).map { it.asSession() }
    }

    override suspend fun getNotUploadedSessions(userId: String): List<Session> {
        return sessionDao.getNotUploadedSessionWithPoints(userId).map { it.asSession() }
    }

    override suspend fun getFeedbackFormOptions(): List<FeedBackFormOption> {
        return sessionApi.getRevisionType().map { FeedBackFormOption(it.id, it.value) }
    }

    override suspend fun getPointFeedbackFormId(): Int{
        return sessionApi.getRevisionType().firstOrNull { it.name == "POINTS" }?.id ?: 2
    }
    private fun getMockedFeedbackForm(): List<FeedBackFormOption>{
        return listOf(
            FeedBackFormOption(0, "Problemi di connessione al sensore Lis Move"),
            FeedBackFormOption(1, "La sessione mostra +/- km rispetto a quanti percorsi effettivamente"),
            FeedBackFormOption(2, "Calcolo dei punti errat"),
            FeedBackFormOption(3, "Problemi di connessione al sensore Lis Move"),
            FeedBackFormOption(4, "La sessione mostra +/- km rispetto a quanti percorsi effettivamente"),
            FeedBackFormOption(5, "Calcolo dei punti errat")
        )
    }

    override suspend fun requestSessionVerification(
        sessionId: String,
        reason: String?,
        userId: String,
        types: List<Int>
    ): Session {
        //TODO: ADD TYPES HERE

        val debugLog = debugLogRepository.getDebugLogEntriesAsString()

        val sessionRequest = SessionValidationRequest(sessionId,true, reason, types, null).apply {
            if (debugLog.isNotBlank()) {
                verificationRequiredExtra = debugLog
            }
        }

        val updatedSession = sessionApi.requestSessionVerification(sessionId, sessionRequest)
        addOrUpdateSessionOnRoom(updatedSession,userId)
        return updatedSession
    }

    private suspend fun addOrUpdateSessionOnRoom(session: Session, userId: String){
        sessionDao.addOrReplace(session.asSessionDataEntity().copy(userId = userId))
        organizationSessionPointDao.addOrUpdatePoints(session.sessionPoints.map { it.asOrganizationSessionPointEntity() })
    }
}