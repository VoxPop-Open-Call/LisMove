package it.lismove.app.android.gaming.ui.parser

import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization

fun EnrollmentWithOrganization.asActiveAwardsSimpleItem(): SimpleItem{
    return SimpleItem(
        organization.id.toString(),
        organization.title,
        "visualizza regolamento completo"
    )
}