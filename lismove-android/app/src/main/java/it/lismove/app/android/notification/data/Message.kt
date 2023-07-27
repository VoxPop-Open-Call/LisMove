package it.lismove.app.android.notification.data

data class Message (
    val body: String,
    val createdDate: Long,
    val id: Long,
    val imageUrl: String?,
    val organization: Long,
    val title: String
)