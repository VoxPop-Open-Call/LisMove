package it.lismove.app.android.session.apiService

import it.lismove.app.android.EnumDto
import it.lismove.app.android.session.data.Session
import retrofit2.http.*

interface SessionApi {
    @POST("/sessions")
    suspend fun createSession(@Body dto: SessionRequest): Session

    @GET("/sessions/{uuid}")
    suspend fun getSession(
        @Path("uuid") id: String,
        @Query("partials") withPartials: Boolean = false
    ): Session

    @GET("/users/{uid}/sessions")
    suspend fun getUserSessions(
        @Path("uid") id: String,
        @Query("start")start: String = "",
        @Query("end")end: String = ""
    ): List<Session>

    @PUT("/sessions/{uuid}")
    suspend fun requestSessionVerification(
        @Path("uuid") id: String,
        @Body dto: SessionValidationRequest
    ): Session

    @GET("/enums/revision-type")
    suspend fun getRevisionType(): List<EnumDto>



}