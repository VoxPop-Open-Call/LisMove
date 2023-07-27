package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.QueryData.OrganizationWithSessionPoint

@Dao
interface OrganizationSessionPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdatePoint(entity: OrganizationSessionPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdatePoints(entity: List<OrganizationSessionPointEntity>)

    @Query("SELECT * FROM OrganizationSessionPointEntity where sessionId = :sessionId")
    suspend fun getSessionPoints(sessionId: String): List<OrganizationSessionPointEntity>

    @Query("SELECT * FROM OrganizationSessionPointEntity where sessionId = :sessionId")
    suspend fun getSessionPointsWithOrganization(sessionId: String): List<OrganizationWithSessionPoint>

    @Query("DELETE FROM OrganizationSessionPointEntity where sessionId = :sessionId")
    suspend fun deleteAll(sessionId: String)
}