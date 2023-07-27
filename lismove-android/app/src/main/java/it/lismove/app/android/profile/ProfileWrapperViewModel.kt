package it.lismove.app.android.profile

import android.app.usage.UsageEvents
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.useCases.LogOutUseCase
import it.lismove.app.android.theme.ThemeRepository
import it.lismove.app.room.LisMoveDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ProfileWrapperViewModel(
    private val logOutUseCase: LogOutUseCase,
): ViewModel() {

    var eventObservable = LiveEvent<EVENT>(LiveEventConfig.PreferFirstObserver)
    var errorObservable = LiveEvent<String>(LiveEventConfig.PreferFirstObserver)


    fun logout(){
        viewModelScope.launch {
            try {
                eventObservable.value = EVENT.SHOW_LOADING
                logOutUseCase.logOut(false)
                eventObservable.value = EVENT.HIDE_LOADING
                eventObservable.value = EVENT.LOGOUT
            }catch (e: Exception){
                eventObservable.value = EVENT.HIDE_LOADING
                errorObservable.value = e.localizedMessage
            }

        }
    }
}

enum class EVENT{
    SHOW_LOADING, HIDE_LOADING, LOGOUT
}
