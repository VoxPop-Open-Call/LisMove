package it.lismove.app.android.dashboard.parser

import it.lismove.app.android.dashboard.data.UserDashboardResponse
import it.lismove.app.android.dashboard.data.UserDistanceStats
import it.lismove.app.room.entity.DashboardEntity
import it.lismove.app.room.entity.DashboardUserDistanceStatsEntity
import it.lismove.app.room.entity.QueryData.DashboardEntityWithDailyDistance

fun DashboardEntityWithDailyDistance.asUserDashboardResponse(): UserDashboardResponse{
    return UserDashboardResponse(
        co2 = dashboardEntity.co2,
        distance = dashboardEntity.distance,
        euro = dashboardEntity.euro,
        sessionNumber = dashboardEntity.sessionNumber,
        messages = dashboardEntity.messages,
        dailyDistance = dailyDistance.map { it.asUserDistanceStats() },
        sessionDistanceAvg = dashboardEntity.sessionDistanceAvg
    )
}

fun DashboardUserDistanceStatsEntity.asUserDistanceStats(): UserDistanceStats{
    return UserDistanceStats(day,distance)
}

fun UserDistanceStats.asDasboardUserDistanceStatsEntity(uid: String): DashboardUserDistanceStatsEntity{
    return DashboardUserDistanceStatsEntity(uid,day,distance)
}

fun UserDashboardResponse.asDashboardEntityWithPoints(uid: String): DashboardEntityWithDailyDistance{
    return DashboardEntityWithDailyDistance(
        dashboardEntity = DashboardEntity(
            userId = uid,
            co2 = co2,
            distance = distance,
            euro = euro,
            sessionNumber = sessionNumber,
            messages = messages,
            sessionDistanceAvg = sessionDistanceAvg
        ),
        dailyDistance = dailyDistance.map { it.asDasboardUserDistanceStatsEntity(uid)}
    )
}