package it.lismove.app.android.initiative.apiService

import it.lismove.app.android.initiative.apiService.data.CustomFieldValueDao
import it.lismove.app.android.initiative.apiService.data.SettingsResponse
import it.lismove.app.android.initiative.data.CustomField
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.SeatEntity
import retrofit2.http.*

interface OrganizationApi {
    @GET("/organizations/{oid}")
    suspend fun getOrganization(@Path("oid")oid: Long): OrganizationEntity

    @GET("/organizations/{oid}/seats")
    suspend fun getSeats(@Path("oid")oid: Long): List<SeatEntity>

    @GET("/organizations/{oid}/custom-fields")
    suspend fun getCustomField(@Path("oid")oid: Long): List<CustomField>

    @POST("/organizations/{oid}/custom-field-values")
    suspend fun createCustomFieldValue(@Path("oid")oid: Long, @Body body: CustomFieldValueDao): CustomFieldValueDao

    @GET("/organizations/{oid}/custom-field-values")
    suspend fun getCustomFiledValue(@Path("oid")oid: Long, @Query("eid")eid: Long): List<CustomFieldValueDao>

    @GET("/organizations/{oid}/settings")
    suspend fun getSettings(@Path("oid")oid: Long): List<SettingsResponse>
}