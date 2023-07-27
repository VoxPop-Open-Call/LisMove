package it.lismove.app.room.dao

import androidx.room.*
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings

@Dao
interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: EnrollmentEntity)

    @Query("SELECT * FROM enrollmententity WHERE id = :id")
    suspend fun getEnrollment(id: Long): EnrollmentEntity

    @Query("DELETE FROM enrollmententity WHERE id = :id")
    fun deleteEnrollment(id: Long)

    @Query("DELETE FROM enrollmententity")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM enrollmententity WHERE user = :uid ")
    suspend fun getEnrollmentsWithOrganizationForUser(uid: String): List<EnrollmentWithOrganization>

    @Transaction
    @Query("SELECT * FROM enrollmententity WHERE user = :uid and endDate >= :date")
    suspend fun getActiveEnrollmentsWithOrganizationForUser(uid: String, date: Long): List<EnrollmentWithOrganization>

    @Transaction
    @Query("SELECT * FROM enrollmententity WHERE user = :uid and startDate <= :date and endDate >= :date")
    suspend fun getActiveEnrollmentsWithOrganizationAndSettingsForUser(uid: String, date: Long): List<EnrollmentWithOrganizationAndSettings>


    @Transaction
    @Query("SELECT * FROM enrollmententity WHERE id = :id ")
    suspend fun getEnrollmentWithOrganization(id: String): EnrollmentWithOrganization
}