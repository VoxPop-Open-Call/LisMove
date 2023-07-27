package it.lismove.app.android.session.repository

import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.session.apiService.SessionRequest
import it.lismove.app.android.session.data.FeedBackFormOption
import it.lismove.app.android.session.data.Session
import it.lismove.app.room.entity.QueryData.SessionWithPoints
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ApplicationSessionRepository{
    suspend fun createSession(session: SessionRequest): Session
    suspend fun getSession(sessionId: String, userId: String): Session
    suspend fun getSessionWithPartials(sessionId: String): Session
    suspend fun getSessionList(userId: String, start: Date, end: Date): List<Session>
    suspend fun getNotUploadedSessions(userId: String): List<Session>
    suspend fun getFeedbackFormOptions(): List<FeedBackFormOption>
    suspend fun getPointFeedbackFormId(): Int
    suspend fun requestSessionVerification(
        sessionId: String,
        reason: String?,
        userId: String,
        types: List<Int> = listOf()
    ): Session
}