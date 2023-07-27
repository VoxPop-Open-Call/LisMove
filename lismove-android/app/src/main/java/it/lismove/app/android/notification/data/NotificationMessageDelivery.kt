package it.lismove.app.android.notification.data

data class NotificationMessageDelivery(
    val body: String? = "",
    val createdDate: Long,
    val message:  Long,
    val imageUrl: String? = null,
    val organization: Long?,
    var read: Boolean? = false,
    val sent: Boolean? = true,
    val title: String? = "",
    val uid: String,
    val username: String = ""
){
    companion object{
        const val NOTIFICATION_ID_KEY = "messageId"
    }
}