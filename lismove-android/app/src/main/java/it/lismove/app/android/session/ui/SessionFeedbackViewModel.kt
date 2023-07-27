package it.lismove.app.android.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.session.data.FeedBackFormOption
import it.lismove.app.android.session.repository.ApplicationSessionRepository
import it.lismove.app.android.session.ui.useCase.SessionDetailUseCase
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lv.chi.photopicker.utils.SingleLiveEvent

class SessionFeedbackViewModel(
     val repository: ApplicationSessionRepository,
     val userCase: SessionDetailUseCase,
     val user: LisMoveUser
): ViewModel() {
     var sessionId: String = ""
     var options: List<FeedBackFormOption> = listOf()

     private var feedBackFormOptions: MutableStateFlow<Lce<List<FeedBackFormOption>>> = MutableStateFlow(LceSuccess(
          listOf()))
     var optionsObservable = feedBackFormOptions.asLiveData()

     var showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
     var showSuccess: SingleLiveEvent<Boolean> = SingleLiveEvent()
     var showError: SingleLiveEvent<String> = SingleLiveEvent()

     fun initViewModel(sessionId: String){
          this.sessionId = sessionId
          loadOptions()
     }

     private fun loadOptions(){
          viewModelScope.launch {
               feedBackFormOptions.emit(LceLoading())
               try {
                    setOptionsAndEmit(repository.getFeedbackFormOptions())
               }catch (e: Exception){
                    feedBackFormOptions.emit(LceError(e))
               }

          }
     }
     private suspend fun setOptionsAndEmit(options: List<FeedBackFormOption>){
          this.options = options
          feedBackFormOptions.emit(LceSuccess(options))
     }

     fun sendRequest(checkedIds: MutableList<Int>, notes: String = "") {
          viewModelScope.launch {
               showLoading.postValue(true)
               withContext(Dispatchers.IO){
                    try {
                         userCase.requestVerification(sessionId, user.uid, notes, checkedIds)
                         showSuccess.postValue(true)
                    }catch (e: Exception){
                         showError.postValue("Si è verificato un errore nell'invio della segnalazione. Riprova più tardi")

                    }
               }

          }
     }
}