package it.lismove.app.android.authentication.repository

import android.net.Uri
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.car.data.CarModification
import it.lismove.app.android.car.data.CarModificationExpanded
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.data.UserCustomField
import it.lismove.app.android.notification.data.NotificationMessageDelivery
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SeatEntity

interface UserRepository {
    suspend fun createUserProfile(uid: String, email: String): LisMoveUser
    suspend fun fetchUserProfile(uid: String): LisMoveUser
    suspend fun getCachedUserProfile(uid: String): LisMoveUser?
    suspend fun existsUser(email: String): Boolean
    suspend fun userNeedsResetPassword(email: String): Boolean
    suspend fun updateUserImage(image: Uri, user: LisMoveUser): LisMoveUser
    suspend fun updateUserProfile(user: LisMoveUser): LisMoveUser
    suspend fun fetchUserProfileFromServer(uid: String): LisMoveUser
    suspend fun consumeCode(uid: String, code: String): EnrollmentEntity?
    suspend fun verifyCode(uid: String, code: String): EnrollmentEntity?
    suspend fun getInitiatives(uid: String): List<EnrollmentWithOrganization>
    suspend fun getActiveInitiatives(uid: String, date: Long = DateTimeUtils.getCurrentTimestamp()):
            List<EnrollmentWithOrganization>
    suspend fun createSeat(seatEntity: SeatEntity, user: LisMoveUser): SeatEntity
    suspend fun getUserCar(uid: String): CarModificationExpanded?
    suspend fun setUserCar(uid: String, carModification: CarModification)
    suspend fun deleteUserCar(uid: String)
    suspend fun getUserCustomField(enrollmentId: String, oid: Long): List<UserCustomField>
    suspend fun getMessages(uid: String): List<NotificationMessageDelivery>
    suspend fun markMessageAsRead(uid: String, mId: Long)
    suspend fun getAwards(uid: String): List<Award>
    suspend fun getActiveInitiativesWithSettings(
        uid: String,
        date: Long = DateTimeUtils.getCurrentTimestamp()
    ): List<EnrollmentWithOrganizationAndSettings>
}