
package it.lismove.app.android.authentication.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.apiService.UserApi
import it.lismove.app.android.authentication.repository.parser.asLisMoveUser
import it.lismove.app.android.authentication.repository.parser.asLisMoveUserEntity
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.car.data.CarModification
import it.lismove.app.android.car.data.CarModificationExpanded
import it.lismove.app.android.general.network.LismoveNetworkException
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.notification.data.NotificationMessageDelivery
import it.lismove.app.room.dao.EnrollmentDao
import it.lismove.app.room.dao.OrganizationDao
import it.lismove.app.room.dao.LisMoveUserEntityDao
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SeatEntity
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.EOFException
import java.io.IOException

class UserRepositoryImpl(
    private val userApi: UserApi,
    private val userDao: LisMoveUserEntityDao,
    private val organizationDao: OrganizationDao,
    private val enrollmentDao: EnrollmentDao,
    private val organizationRepository: OrganizationRepository,
    private val cityRepository: CityRepository
): UserRepository {


    override suspend fun createUserProfile(uid: String, email: String): LisMoveUser {
        val user =  LisMoveUser(uid = uid, email = email )
        val userResponse = userApi.createUser(user)
        userDao.addOrUpdate(userResponse.asLisMoveUserEntity())
        return userResponse
    }

    override suspend fun fetchUserProfileFromServer(uid: String): LisMoveUser {
        val user = userApi.getUser(uid)
        user.homeCity?.let { user.homeCityExtended = cityRepository.getCity(it) }
        user.workAddresses?.forEach { seat ->
            seat.city?.let {
                seat.cityExtended = cityRepository.getCity(it)
            }
        }
        return user

    }
    override suspend fun consumeCode(uid: String, code: String): EnrollmentEntity{
            return userApi.consumeCode(uid, code)
    }

    override suspend fun verifyCode(uid: String, code: String): EnrollmentEntity?{
        return try {
            userApi.verifyCode(uid, code)
        }catch (e: LismoveNetworkException){
            if(e.status == 404){
                null
            }else{
                throw e
            }
        }
    }

    override suspend fun getInitiatives(uid: String): List<EnrollmentWithOrganization> {
        try {
            var res: ArrayList<EnrollmentWithOrganization> = arrayListOf()
            userApi.getEnrollments(uid).forEach {
                val organization = organizationRepository.getOrganization(it.organization)
                enrollmentDao.addOrUpdate(it.copy(user = uid))
                res.add(EnrollmentWithOrganization(it,organization ))
            }
            return res
        }catch (e: IOException){
            Timber.d("getting cached")
            return enrollmentDao.getEnrollmentsWithOrganizationForUser(uid)
        }
    }

    override suspend fun getActiveInitiatives(uid: String, date: Long): List<EnrollmentWithOrganization> {
        syncEnrollmentAndOrganizationIfNetworkAvailable(uid)
        return enrollmentDao.getActiveEnrollmentsWithOrganizationForUser(uid, date)
    }



    override suspend fun fetchUserProfile(uid: String): LisMoveUser {
        val cachedUser = userDao.getUser(uid)
        try{
            val user = userApi.getUser(uid)
            userDao.addOrUpdate(user.asLisMoveUserEntity())
            user.homeCity?.let { user.homeCityExtended = cityRepository.getCity(it) }
            user.workAddresses?.forEach { seat ->
                seat.city?.let {
                    seat.cityExtended = cityRepository.getCity(it)
                }
            }
            return user
        }catch (e: IOException){
            Timber.d("Getting fetched data")
            return cachedUser.asLisMoveUser()
        }

    }

    override suspend fun getCachedUserProfile(uid: String): LisMoveUser? {
        return try {
            userDao.getUser(uid).asLisMoveUser()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(user: LisMoveUser): LisMoveUser {
        var user = user
        user = userApi.updateUser(user.uid, user)
        user.homeCity?.let { user.homeCityExtended = cityRepository.getCity(it) }
        userDao.addOrUpdate(user.asLisMoveUserEntity())
        user.workAddresses?.forEach { seat ->
            seat.city?.let {
                seat.cityExtended = cityRepository.getCity(it)
            }
        }
        return user
    }

    override suspend fun getActiveInitiativesWithSettings(uid: String, date: Long): List<EnrollmentWithOrganizationAndSettings> {
        syncEnrollmentAndOrganizationIfNetworkAvailable(uid)

        return enrollmentDao.getActiveEnrollmentsWithOrganizationAndSettingsForUser(uid, DateTimeUtils.getCurrentTimestamp())
    }


    private suspend fun syncEnrollmentAndOrganizationIfNetworkAvailable(uid: String){
        try {
            var res: ArrayList<EnrollmentWithOrganization> = arrayListOf()
            userApi.getEnrollments(uid).forEach {
                val organization = organizationRepository.getOrganization(it.organization)
                enrollmentDao.addOrUpdate(it.copy(user = uid))
                res.add(EnrollmentWithOrganization(it,organization ))
                organizationRepository.getSettings(oid = it.organization)
            }
        }catch (e: LismoveNetworkException){
            throw e
        } catch (e: IOException){
            Timber.d("getting cached")
        }
    }
    override suspend fun updateUserImage(image: Uri, user: LisMoveUser): LisMoveUser {
        val url = uploadImage(image, user.uid)
        Timber.d("Update user progfile with url: $url")
        return updateUserProfile( user.copy(avatarUrl = url))
    }

    override suspend fun existsUser(email: String): Boolean {
        return userApi.userExists(email)
    }

    override suspend fun userNeedsResetPassword(email: String): Boolean{
        return userApi.resetPassword(email)
    }


    private suspend fun uploadImage(localUri: Uri, uid: String): String {
        val name = "${DateTimeUtils.getCurrentTimestamp()}"
        val extension: String = localUri.toString().substring(localUri.toString().lastIndexOf(".") + 1)
        val storageRefString =
            "users/avatars/$uid/$name.$extension"
        val storageRef =   FirebaseStorage.getInstance().getReference(storageRefString)
        storageRef.putFile(localUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    override suspend fun createSeat(seatEntity: SeatEntity, user: LisMoveUser): SeatEntity{
        return userApi.requestSeat(user.uid, seatEntity.copy(id = null))
    }

    override suspend fun getUserCustomField(enrollmentId: String, oid: Long): List<UserCustomField>
    {
        return organizationRepository.getUserCustomField(enrollmentId.toLong(), oid) ?: listOf()
    }

    override suspend fun getMessages(uid: String): List<NotificationMessageDelivery> {
        return userApi.getMessages(uid).sortedByDescending { it.createdDate }
    }

    override suspend fun markMessageAsRead(uid: String, mId: Long) {
         userApi.markMessageAsRead(uid, mId.toString())
    }

    override suspend fun getUserCar(uid: String): CarModificationExpanded? {
        try {
            return userApi.getUserCar(uid)
        }catch (e: EOFException){
            // The api call return empty body for null objects
            return null
        }
    }

    override suspend fun setUserCar(uid: String, carModification: CarModification) {
         userApi.setUserCar(uid, carModification)
    }

    override suspend fun deleteUserCar(uid: String) {
        return userApi.deleteUserCar(uid)
    }

    override  suspend fun getAwards(uid: String): List<Award>{
        return userApi.getUserAwards(uid)
    }


}
