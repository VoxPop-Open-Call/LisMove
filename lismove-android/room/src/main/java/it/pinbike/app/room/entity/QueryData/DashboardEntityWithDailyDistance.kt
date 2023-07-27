package it.lismove.app.room.entity.QueryData

import androidx.room.Embedded
import androidx.room.Relation
import it.lismove.app.room.entity.DashboardEntity
import it.lismove.app.room.entity.DashboardUserDistanceStatsEntity
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity

data class DashboardEntityWithDailyDistance(
    @Embedded
    val dashboardEntity: DashboardEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val dailyDistance: List<DashboardUserDistanceStatsEntity>
)