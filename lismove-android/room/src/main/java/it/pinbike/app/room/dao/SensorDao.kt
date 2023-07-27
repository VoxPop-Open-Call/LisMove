package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.SensorEntity
import it.lismove.app.room.entity.SessionDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: SensorEntity)

    @Query("Select * from sensorentity where userId =:userUid")
    suspend fun getSensor(userUid: String): SensorEntity

    @Query("Select * from sensorentity where userId =:userUid")
    fun getSensorObservable(userUid: String): Flow<SensorEntity?>

    @Query("delete from sensorentity where userId = :userUid")
    fun deleteSensor(userUid: String)
}