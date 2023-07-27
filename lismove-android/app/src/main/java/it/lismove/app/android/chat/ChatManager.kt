package it.lismove.app.android.chat

import it.lismove.app.room.entity.LisMoveUser

interface ChatManager {
    fun setUser(user: LisMoveUser)

    fun isEnabled(): Boolean

    suspend fun getOpenedChatOrNull(): String?

    fun openNewChat(message: String)

    fun openChat(id: String)

    fun closeChat(id: String)


}