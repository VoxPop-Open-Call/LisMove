package it.lismove.app.android.authentication.apiService

import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.car.data.CarModification
import it.lismove.app.android.car.data.CarModificationExpanded
import it.lismove.app.android.dashboard.data.UserDashboardResponse
import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.android.gaming.apiService.data.Ranking
import it.lismove.app.android.notification.data.NotificationMessageDelivery
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.SeatEntity
import retrofit2.http.*

interface UserApi {
    @POST("/users")
    suspend fun createUser(
        @Body user: LisMoveUser
    ): LisMoveUser

    @GET("/users/{email}/exists")
    suspend fun userExists(
        @Path(value = "email") email: String): Boolean

    @Headers("No-Authentication: true")
    @GET("/users/{email}/reset-password")
    suspend fun resetPassword(
        @Path(value = "email") email: String): Boolean

    @GET("/users/{uid}/dashboard")
    suspend fun getDashboard(@Path(value = "uid") uid: String): UserDashboardResponse

    @GET("/users/{uid}")
    suspend fun getUser(
        @Path(value = "uid") uid: String): LisMoveUser

    @PUT("/users/{uid}")
    suspend fun updateUser(
        @Path(value = "uid") uid: String,
        @Body user: LisMoveUser
    ): LisMoveUser

    @GET("/users/{uid}/consume/{code}")
    suspend fun consumeCode(@Path("uid")id: String,
                            @Path("code") code: String): EnrollmentEntity

    @GET("/users/{uid}/verify/{code}")
    suspend fun verifyCode(@Path("uid")id: String,
                            @Path("code") code: String): EnrollmentEntity

    @GET("/users/{uid}/enrollments")
    suspend fun getEnrollments(@Path("uid")id: String): List<EnrollmentEntity>

    @POST("/users/{uid}/seats")
    suspend fun requestSeat(@Path("uid")id: String, @Body seatEntity: SeatEntity): SeatEntity

    @GET("/users/{uid}/rankings")
    suspend fun getRankings(@Path("uid")uid: String): List<Ranking>

    @GET("/users/{uid}/achievements")
    suspend fun getAchievements(@Path("uid")uid: String): List<Achievement>

    @GET("/users/{uid}/car")
    suspend fun getUserCar(@Path("uid")uid: String): CarModificationExpanded?

    @POST("/users/{uid}/car")
    suspend fun setUserCar(@Path("uid")uid: String,
                           @Body carModification: CarModification)
    @DELETE(("/users/{uid}/car"))
    suspend fun deleteUserCar(@Path("uid")uid: String)

    @GET("/users/{uid}/messages")
    suspend fun getMessages(
        @Path("uid")uid: String
    ): List<NotificationMessageDelivery>

    @GET("/users/{uid}/messages/{mid}")
    suspend fun markMessageAsRead(
        @Path("uid")uid: String,
        @Path("mid")mid: String
    ): NotificationMessageDelivery

    @GET("/users/{uid}/awards")
    suspend fun getUserAwards(
        @Path("uid")uid: String
    ): List<Award>
}