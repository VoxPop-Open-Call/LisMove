package it.lismove.app.android.authentication.useCases.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.useCases.LogOutUseCase
import it.lismove.app.android.general.LisMoveAppSettings
import it.lismove.app.android.theme.ThemeRepository
import it.lismove.app.room.LisMoveDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.database.SessionSdkRepository
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class LogOutUseCaseImpl(
    val context: Context,
    private val authRepository: AuthRepository,
    private val themeRepository: ThemeRepository,
    private val sessionRepository: SessionSdkRepository,
    private val sensorSdk: LismoveSensorSdk,
    private val database: LisMoveDatabase
): LogOutUseCase {

    override suspend fun logOut(forceStopSession: Boolean) {
        Timber.d("Performing logout")
        val activeSession = sessionRepository.getActiveSession()
        if(activeSession != null) {
            if(forceStopSession){
                sensorSdk.stop(activeSession.id)
            }else{
                throw Exception("Non è possibile effettuare il logout durante una sessione. Termina prima la sessione in corso e riprova")
            }
        }

        authRepository.signOut()
        themeRepository.resetTheme()
        context.getSharedPreferences(LisMoveAppSettings.SHARED_PREFERENCES_KEY, 0).edit {
            clear()
        }
        withContext(Dispatchers.IO){
            database.clearAllTables()
        }
        delay(2000)
    }
}