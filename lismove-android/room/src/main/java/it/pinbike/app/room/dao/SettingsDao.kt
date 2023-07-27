package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.SettingsEntity

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: SettingsEntity)

    @Query("DELETE FROM settingsentity")
    fun deleteAll()

}