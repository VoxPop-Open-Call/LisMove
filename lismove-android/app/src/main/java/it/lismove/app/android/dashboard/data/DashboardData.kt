package it.lismove.app.android.dashboard.data

import it.lismove.app.android.dashboard.itemViews.data.ChartPointData
import it.lismove.app.android.dashboard.itemViews.data.SensorItemData
import it.lismove.app.room.entity.DashoardPositionEntity

data class DashboardData(
    var nickname: String? = null,
    var userAvatar: String? = null,
    val sessionNumber: Int? = null,
    val messages: Int? = null,
    val euroReceived: Double? = null,
    val activeProjectsImages: List<ActiveInitiativeData>? = null,
    val initiativePoints: List<RankingPointData>? = null,
    val totalKm: Double? = null,
    val co2: Double? = null,
    val dailyDistance: List<ChartPointData>? = null,
    var itemPositions: List<DashoardPositionEntity>,
    var sensorList: List<SensorItemData>? = null
){
    fun getItemPosition(id: Int) = itemPositions.first { it.dashboardItemId == id }.dashboardPosition
}