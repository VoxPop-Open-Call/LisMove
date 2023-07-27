package it.lismove.app.android.initiative.repository

import it.lismove.app.android.authentication.repository.CityRepository
import it.lismove.app.android.initiative.apiService.OrganizationApi
import it.lismove.app.android.initiative.data.CustomField
import it.lismove.app.room.entity.SettingsEntity
import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.android.initiative.parser.asCustomFieldValueDao
import it.lismove.app.android.initiative.parser.asSettingsDao
import it.lismove.app.room.dao.OrganizationDao
import it.lismove.app.room.dao.SettingsDao
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.SeatEntity
import timber.log.Timber
import java.io.IOException

class OrganizationRepositoryImpl(
    private val organizationApi: OrganizationApi,
    private val organizationDao: OrganizationDao,
    private val cityRepository: CityRepository,
    private val settingsDao: SettingsDao
): OrganizationRepository {


    override suspend fun getOrganization(oid: Long): OrganizationEntity {
        try {
            val organization =  organizationApi.getOrganization(oid)
            organizationDao.addOrUpdate(organization)
            return organization
        }catch (e: IOException){
            Timber.d("No internet connection, cached")
            return organizationDao.getOrganization(oid)
        }
    }

    override suspend fun getSeats(oid: Long): List<SeatEntity> {
        val seats = organizationApi.getSeats(oid)
        seats.forEach {seat ->
            seat.city?.let {
                seat.cityExtended =  cityRepository.getCity(it)
            }
        }
        return seats
    }

    private suspend fun getCustomField(oid: Long): List<CustomField> {
        return organizationApi.getCustomField(oid).filter { !it.name.isNullOrEmpty() }
    }

    override suspend fun getUserCustomField(eid: Long, oid: Long): List<UserCustomField> {
        val organizationCustomField = getCustomField(oid)
        val userCustomField = organizationApi.getCustomFiledValue(oid, eid)
        return organizationCustomField.map { cf ->
            UserCustomField(
                customFieldId = cf.id,
                description = cf.description,
                name = cf.name,
                organization = cf.organization,
                type = cf.type,
                value = userCustomField.firstOrNull { it.customField == cf.id }?.value ?: false,
                eid = eid)
        }
    }

    override suspend fun setUserCustomField(userCustomField: UserCustomField): UserCustomField {
        val res = organizationApi.createCustomFieldValue(userCustomField.organization, userCustomField.asCustomFieldValueDao())
        userCustomField.value = res.value
        return userCustomField
    }

    override suspend fun getSettings(oid: Long): SettingsEntity {
        val res = organizationApi.getSettings(oid).asSettingsDao(oid)
        settingsDao.addOrUpdate(res)
        return  res
    }


}