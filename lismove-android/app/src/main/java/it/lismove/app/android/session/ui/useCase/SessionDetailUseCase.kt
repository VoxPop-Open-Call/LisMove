package it.lismove.app.android.session.ui.useCase

import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.data.SessionPoint
import it.lismove.app.android.session.ui.data.SessionDetailUI
import kotlinx.coroutines.flow.Flow

interface SessionDetailUseCase {
    suspend fun getSession(id: String, uid: String): Session
    suspend fun getSessionPoints(sessionId: String,  userId: String): List<ListAlertData>
    suspend fun requestVerification(sessionId: String, userId: String, reason: String, types: List<Int> = listOf()): Session
    suspend fun requestPointVerification(sessionId: String, userId: String, reason: String): Session
    suspend fun getSessionWithPartials(sessionId: String): Session
}