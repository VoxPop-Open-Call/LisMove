package it.lismove.app.android.initiative.repository

import it.lismove.app.room.entity.SettingsEntity
import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.SeatEntity

interface OrganizationRepository {
    suspend fun getOrganization(oid: Long): OrganizationEntity
    suspend fun getSeats(oid: Long): List<SeatEntity>
    suspend fun getUserCustomField(eid: Long, oid: Long): List<UserCustomField>?
    suspend fun setUserCustomField(userCustomField: UserCustomField): UserCustomField
    suspend fun getSettings(oid: Long): SettingsEntity
}