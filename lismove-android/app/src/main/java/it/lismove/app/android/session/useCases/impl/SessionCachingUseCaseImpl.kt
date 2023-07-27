package it.lismove.app.android.session.useCases.impl

import it.lismove.app.android.session.repository.ApplicationSessionRepository
import it.lismove.app.android.session.useCases.SessionCachingUseCase
import it.lismove.app.android.session.useCases.SessionUploadUseCase
import timber.log.Timber
import java.lang.Exception

class SessionCachingUseCaseImpl(
    private val sessionRepository: ApplicationSessionRepository,
    private val sessionUploadUseCase: SessionUploadUseCase
): SessionCachingUseCase {

    override suspend fun sendNotUploadedSessions(userId: String): String? {
        val notUploadedSessions = sessionRepository.getNotUploadedSessions(userId)
        Timber.d("Found ${notUploadedSessions.size} not updated session")
        var sessionUpdated = 0
        notUploadedSessions.forEach {
            try{
                sessionUploadUseCase.uploadSession(userId, it.id.toString())
                sessionUpdated += 1
                Timber.d("Session correctly uploaded")

            }catch (e: Exception){
                Timber.d("Tried to upload a cached session but received error, ${e.message}")
            }
        }
        return if(sessionUpdated == 0){
            null
        }else{
            "Sono state inviate correttamente $sessionUpdated sessioni"
        }
    }
}