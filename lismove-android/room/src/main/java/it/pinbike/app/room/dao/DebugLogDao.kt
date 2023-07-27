package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.DebugLogEntity

@Dao
interface DebugLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEntry(entry: DebugLogEntity)

    @Query("SELECT * FROM debuglogentity")
    suspend fun getAll(): List<DebugLogEntity>

    @Query("DELETE FROM debuglogentity")
    suspend fun deleteAll()

    @Query("DELETE FROM debuglogentity where timestamp < :ts")
    suspend fun deleteBefore(ts: Long)
}