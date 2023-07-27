package it.lismove.app.room.dao

import androidx.room.*
import it.lismove.app.room.entity.DashoardPositionEntity

@Dao
interface DashboardPositionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: DashoardPositionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateAll(entity: List<DashoardPositionEntity>)

    @Query("SELECT * FROM dashoardpositionentity WHERE dashboardItemId = :id")
    suspend fun getDashboardPosition(id: Int): DashoardPositionEntity

    @Query("SELECT * FROM dashoardpositionentity")
    suspend fun getDashboardPositionList(): List<DashoardPositionEntity>

    @Query("DELETE FROM dashoardpositionentity")
    suspend fun deleteAll()

}