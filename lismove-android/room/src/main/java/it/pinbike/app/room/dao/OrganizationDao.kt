package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.OrganizationEntity

@Dao
interface OrganizationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: OrganizationEntity)

    @Query("SELECT * FROM organizationentity WHERE id = :oid")
    suspend fun getOrganization(oid: Long): OrganizationEntity

    @Query("DELETE FROM organizationentity WHERE id = :oid")
    fun deleteOrganization(oid: Long)

    @Query("DELETE FROM organizationentity")
    fun deleteAll()

}