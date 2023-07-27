package it.lismove.app.android.dashboard.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import it.lismove.app.android.authentication.apiService.UserApi
import it.lismove.app.android.dashboard.data.UserDashboardResponse
import it.lismove.app.android.dashboard.parser.asDashboardEntityWithPoints
import it.lismove.app.android.dashboard.parser.asUserDashboardResponse
import it.lismove.app.android.general.LisMoveAppSettings
import it.lismove.app.room.dao.DashboardDao
import it.lismove.app.room.dao.DashboardPositionDao
import it.lismove.app.room.entity.DashoardPositionEntity
import timber.log.Timber
import java.io.IOException

class DashboardRepositoryImpl(
    val userApi: UserApi,
    val dashboardPositionDao: DashboardPositionDao,
    val dashboardDao: DashboardDao,
    val context: Context
): DashboardRepository {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = LisMoveAppSettings.SHARED_PREFERENCES_KEY
    private val dashboardVersionKey = "dashboard_version"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private val dashboardVersion = 5
    private val deviceDashboardVersion: Int
        get() = sharedPref.getInt(dashboardVersionKey, -1)

    override suspend fun getDashboardItemPositions(): List<DashoardPositionEntity> {
        val positions = dashboardPositionDao.getDashboardPositionList()
        if(deviceDashboardVersion != dashboardVersion || positions.isEmpty()){
            Timber.d("Creating default positions")
         updateDeviceDashboardVersion()
         return populateDefaultItemPosition()
        }
        return positions
    }

    private fun updateDeviceDashboardVersion(){
        sharedPref.edit{
            putInt(dashboardVersionKey, dashboardVersion)
        }
    }

    suspend fun populateDefaultItemPosition(): List<DashoardPositionEntity>{
        val defaultItemPositions = listOf(
            DashoardPositionEntity(DashoardPositionEntity.SENSOR, 0),
            DashoardPositionEntity(DashoardPositionEntity.PROFILE, 1),
            DashoardPositionEntity(DashoardPositionEntity.MESSAGES, 8),
            DashoardPositionEntity(DashoardPositionEntity.EUROS, 2),
            DashoardPositionEntity(DashoardPositionEntity.PROJECTS, 3),
            DashoardPositionEntity(DashoardPositionEntity.KM_DONE, 4),
            DashoardPositionEntity(DashoardPositionEntity.CO2, 5),
            DashoardPositionEntity(DashoardPositionEntity.POINTS, 6),
            DashoardPositionEntity(DashoardPositionEntity.USAGE, 7),
            )
        dashboardPositionDao.deleteAll()
        dashboardPositionDao.addOrUpdateAll(defaultItemPositions)
        return defaultItemPositions
    }
    override suspend fun getDashboard(uid: String): UserDashboardResponse {
        try {
            val dashboard = userApi.getDashboard(uid)
            dashboardDao.add(dashboard.asDashboardEntityWithPoints(uid))
            return dashboard
        }catch (e: IOException){
            return dashboardDao.getDashboardPosition(uid).asUserDashboardResponse()
        }
    }

    override suspend fun updateDashboardItemPositions(positions: List<DashoardPositionEntity>){
        dashboardPositionDao.addOrUpdateAll(positions)
    }


}