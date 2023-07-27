package it.lismove.app.android.authentication.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.authentication.useCases.AuthenticationUseCase
import it.lismove.app.android.authentication.useCases.LogOutUseCase
import it.lismove.app.android.authentication.useCases.data.LoginState
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import net.nextome.lismove_sdk.location.LisMoveLocationManager
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.core.component.KoinComponent
import timber.log.Timber


class SplashScreenViewModel(
        private val authenticationUseCase: AuthenticationUseCase,
        private val logoutUseCase: LogOutUseCase,
        private val locationManager: LisMoveLocationManager,
): ViewModel(), KoinComponent {

    var startingIntentExtras: Bundle? = null

    fun getAuthenticationState() = flow<Lce<LoginState>> {
        emit(LceLoading())
        emit(LceSuccess(getAuthUseCase()))
    }.catch {
        Timber.e(it)
        BugsnagUtils.reportIssue(it, BugsnagUtils.ErrorSeverity.ERROR)
        logoutUseCase.logOut(true)
        emit(LceError(it))
    }.asLiveData()

    private suspend fun getAuthUseCase(): LoginState{
        return withContext(Dispatchers.IO){
            authenticationUseCase.fetchUserAuthenticationState()
        }
    }


    fun prepareGpsLocationFix(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            GlobalScope.launch {
                // Get a location fix on app start
                // We use GlobalScope to avoid cancelling the location request after activity closes

                val location = locationManager.awaitLastLocationOrNull(context, true)
            }
        }
    }

}