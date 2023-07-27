package it.lismove.app.android.notification.parser

import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.notification.data.NotificationListItem
import it.lismove.app.android.notification.data.NotificationMessageDelivery

fun NotificationMessageDelivery.asNotificationListItem(): NotificationListItem {
    return NotificationListItem(
        title =  title ?: "",
        id = message.toString(),
        body = body ?: "",
        date = DateTimeUtils.getReadableDateTime(createdDate),
        imageUrl = imageUrl,
        seen = read != true,
        isInitiallyOpen = false
    )
}