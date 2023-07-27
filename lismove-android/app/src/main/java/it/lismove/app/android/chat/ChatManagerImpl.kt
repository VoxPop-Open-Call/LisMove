package it.lismove.app.android.chat

import com.zoho.livechat.android.VisitorChat
import com.zoho.livechat.android.ZohoLiveChat
import com.zoho.livechat.android.constants.ConversationType
import com.zoho.livechat.android.listeners.ConversationListener
import it.lismove.app.room.entity.LisMoveUser
import java.lang.Exception
import java.util.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatManagerImpl: ChatManager {

    override fun setUser(user: LisMoveUser){
        ZohoLiveChat.registerVisitor(user.uid)
        ZohoLiveChat.Visitor.setName(user.firstName +" "+ user.lastName)
        ZohoLiveChat.Visitor.setEmail(user.email)
        user.phoneNumber?.let {
            ZohoLiveChat.Visitor.setContactNumber(it)
        }
    }

    override fun isEnabled(): Boolean{
        return ZohoLiveChat.isSDKEnabled()
    }

    override suspend fun getOpenedChatOrNull(): String?{
        return getOpenChatOrNullSuspend()
    }

    override fun openNewChat(message: String){
        ZohoLiveChat.Visitor.startChat(message)
    }

    override fun openChat(id: String){
        ZohoLiveChat.Chat.open(id)
    }

    override fun closeChat(id: String){
        ZohoLiveChat.Chat.endChat(id)
    }

    private suspend fun getOpenChatOrNullSuspend() = suspendCoroutine<String?> { continuation ->
        ZohoLiveChat.Chat.getList(ConversationType.OPEN, object: ConversationListener {
            override fun onSuccess(p0: ArrayList<VisitorChat>?) {
                continuation.resume(p0?.firstOrNull()?.chatID)
            }

            override fun onFailure(p0: Int, p1: String?) {
                continuation.resumeWithException(Exception(p1))
            }

        } )

    }



}