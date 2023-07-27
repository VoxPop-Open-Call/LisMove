package it.lismove.app.room.dao

import androidx.room.*
import it.lismove.app.room.entity.DashboardEntity
import it.lismove.app.room.entity.DashboardUserDistanceStatsEntity
import it.lismove.app.room.entity.QueryData.DashboardEntityWithDailyDistance

@Dao
interface DashboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: DashboardEntity)

    @Transaction
    @Query("SELECT * FROM  dashboardentity WHERE userId = :userId")
    suspend fun getDashboardPosition(userId: String): DashboardEntityWithDailyDistance

    @Transaction
    suspend fun add(entity: DashboardEntityWithDailyDistance){
        addOrUpdate(entity.dashboardEntity)
        clearStatsForUser(entity.dashboardEntity.userId)
        addOrUpdateStats(entity.dailyDistance)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateStats(entity: List<DashboardUserDistanceStatsEntity>)

    @Query("DELETE FROM dashboarduserdistancestatsentity where userId = :userId")
    suspend fun clearStatsForUser(userId: String)
}