package it.lismove.app.android.session.useCases

interface SessionCachingUseCase {
    suspend fun sendNotUploadedSessions(userId: String): String?
}