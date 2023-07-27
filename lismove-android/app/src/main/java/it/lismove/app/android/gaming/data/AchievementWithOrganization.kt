package it.lismove.app.android.gaming.data

import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.room.entity.OrganizationEntity

data class AchievementWithOrganization (
    val achievement: Achievement,
    val organization: OrganizationEntity?
)