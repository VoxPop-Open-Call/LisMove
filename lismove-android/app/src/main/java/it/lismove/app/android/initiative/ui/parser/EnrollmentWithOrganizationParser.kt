package it.lismove.app.android.initiative.ui.parser

import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization

fun EnrollmentWithOrganization.asSimpleItem(): SimpleItem {
    val startDate = DateTimeUtils.getReadableShortDate(enrollment.startDate)
    val endDate = DateTimeUtils.getReadableShortDate(enrollment.endDate)
    return SimpleItem(
        id = enrollment.id.toString(),
        data = organization.title,
        subtitle = "Attiva dal $startDate al $endDate"
    )
}