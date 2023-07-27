package it.lismove.app.android.logWall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.logWall.repository.LogWallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class LogWallViewModel(
    private val logWallRepository: LogWallRepository
): ViewModel() {
    private val stateFlow = MutableStateFlow<Lce<List<String>>>(LceSuccess(listOf()))
    val stateObservable = stateFlow.asLiveData()


    fun reloadLogWallEvents(){
        viewModelScope.launch {
            try{
                stateFlow.emit(LceLoading())
                val events =  logWallRepository.getLogWallEvents()
                stateFlow.emit(LceSuccess(events))
            }catch (e: Exception){
                stateFlow.emit(LceError(e))
            }

        }

    }
}