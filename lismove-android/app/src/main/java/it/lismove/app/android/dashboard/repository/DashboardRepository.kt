package it.lismove.app.android.dashboard.repository

import it.lismove.app.android.dashboard.data.UserDashboardResponse
import it.lismove.app.room.entity.DashoardPositionEntity

interface DashboardRepository {
    suspend fun getDashboardItemPositions(): List<DashoardPositionEntity>
    suspend fun updateDashboardItemPositions(positions: List<DashoardPositionEntity>)
    suspend fun getDashboard(uid: String): UserDashboardResponse
}