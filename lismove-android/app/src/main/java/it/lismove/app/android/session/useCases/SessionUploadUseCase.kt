package it.lismove.app.android.session.useCases

import it.lismove.app.android.session.data.Session

interface SessionUploadUseCase {
    suspend fun  uploadSession(userId: String, sessionId: String): Session
}