package it.lismove.app.android.initiative.ui.data

import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.LisMoveUser

data class InitiativeConfiguration(
    val user: LisMoveUser,
    val initiative: EnrollmentWithOrganization,
    val customField: List<UserCustomField>,
    val customFieldExclusive: Boolean,
    val active: Boolean
)