package it.lismove.app.android.deviceConfiguration.apiService

import it.lismove.app.android.session.data.Session
import it.lismove.app.room.entity.SensorEntity
import retrofit2.http.*

interface SensorApi {
    @POST("/users/{uid}/sensor")
    suspend fun addSensor(@Path("uid")id: String, @Body dto: SensorEntity): SensorEntity

    @GET("/users/{uid}/sensor?active=true")
    suspend fun getActiveSensor(@Path("uid")id: String): SensorEntity

    @GET("/users/{uid}/sensor?active=true")
    suspend fun getActiveSensorList(@Path("uid")id: String): List<SensorEntity>

    @GET("/users/{uid}/sensor/{uuid}/stolen")
    suspend fun setStolen(@Path("uid")id: String, @Path("uuid")uuid: String): SensorEntity

    @DELETE("/users/{uid}/sensor/{uuid}")
    suspend fun disassociateSensor(@Path("uid")id: String, @Path("uuid")uuid: String)

}