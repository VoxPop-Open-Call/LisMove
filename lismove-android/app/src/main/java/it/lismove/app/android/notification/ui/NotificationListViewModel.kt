package it.lismove.app.android.notification.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.notification.data.NotificationListItem
import it.lismove.app.android.notification.data.NotificationMessageDelivery
import it.lismove.app.android.notification.parser.asNotificationListItem
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.utils.BugsnagUtils
import timber.log.Timber
import java.lang.Exception

class NotificationListViewModel(
    val repository: UserRepository,
    val user: LisMoveUser
): ViewModel() {

    private val stateFlow: MutableStateFlow<Lce<List<NotificationListItem>>> = MutableStateFlow(LceSuccess(listOf()))
    val stateObservable = stateFlow.asLiveData()
    var messages: List<NotificationMessageDelivery> = listOf()
    private var initialMessage: Long? = null

    fun reloadMessages(firstTime: Boolean){
        viewModelScope.launch {
            try {
                stateFlow.emit(LceLoading())
                val messages = repository.getMessages(user.uid)
                updateMessageList(messages, firstTime)
            }catch (e: Exception){
                stateFlow.emit(LceError(e))
            }
        }

    }


    private suspend fun updateMessageList(messageList: List<NotificationMessageDelivery>, firstTime: Boolean){
        try {
            messages = messageList
            val messagesUI = messages.map { it.asNotificationListItem() }
            if(firstTime && initialMessage!= null){
                messagesUI.firstOrNull { it.id.toLong() == initialMessage }?.isInitiallyOpen = true
            }
            stateFlow.emit(LceSuccess(messages.map { it.asNotificationListItem() }))
        }catch (e: Exception){
            stateFlow.emit(LceError(Exception(e.localizedMessage ?: "")))
        }

    }

    fun setDataFromIntent(intent: Intent) {
        if(intent.hasExtra(NotificationListActivity.INTENT_NOTIFICATION_OPEN)){
            initialMessage = intent.getLongExtra(NotificationListActivity.INTENT_NOTIFICATION_OPEN, 0)
        }
    }

    fun setNotificationMessageSeen(messageListItem: NotificationListItem){
        try{
            val notification = messages.first { it.message == messageListItem.id.toLong() }
            if(notification.read != null){
                viewModelScope.launch {
                    repository.markMessageAsRead(user.uid, notification.message)
                    messages.first { it.message == messageListItem.id.toLong() }.read = true
                }
            }
        }catch (e: Exception){
            Timber.d("Si è verificato un errore nel sergnalare il messaggio come letto: ${e.localizedMessage ?: ""}")
            BugsnagUtils.reportIssue(e, BugsnagUtils.ErrorSeverity.ERROR)
        }

    }
}