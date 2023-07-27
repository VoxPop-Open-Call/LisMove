package it.lismove.app.android.other

import androidx.lifecycle.ViewModel
import it.lismove.app.android.chat.ChatManager
import it.lismove.app.room.entity.LisMoveUser

class HelpAndFaqViewModel(
    val chat: ChatManager,
    val user: LisMoveUser
):ViewModel() {
    val helpUrl = "https://lismoveadmin.it/help-faq/"

    val isChatEnabled = chat.isEnabled()
}