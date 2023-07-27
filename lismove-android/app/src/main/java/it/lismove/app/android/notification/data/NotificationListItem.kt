package it.lismove.app.android.notification.data

data class NotificationListItem(
    val title: String,
    val id: String,
    val body: String,
    val date: String,
    val imageUrl: String?,
    val seen: Boolean,
    var isInitiallyOpen: Boolean
)