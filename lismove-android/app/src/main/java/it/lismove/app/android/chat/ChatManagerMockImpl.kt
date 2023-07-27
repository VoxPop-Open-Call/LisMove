package it.lismove.app.android.chat

import it.lismove.app.room.entity.LisMoveUser
import timber.log.Timber
import java.util.*

class ChatManagerMockImpl: ChatManager {
    var currentId: String? = "Test1"
    override fun setUser(user: LisMoveUser){

    }

    override fun isEnabled(): Boolean{
        return true
    }

    override suspend fun getOpenedChatOrNull(): String?{
        Timber.d("getOpenedChat $currentId")
        return currentId
    }

    override fun openNewChat(message: String){
        currentId = UUID.randomUUID().toString()
        Timber.d("Creating new chat with id $currentId and message $message")
    }

    override fun openChat(id: String){
        Timber.d("Open chat with id $currentId")
    }

    override fun closeChat(id: String){
        Timber.d("Close chat with id $currentId")

    }


}