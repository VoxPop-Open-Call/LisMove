package it.lismove.app.android.session.apiService

import retrofit2.http.Body
import retrofit2.http.POST

interface SensorHistoryApi {
    @POST("/sessions/offline")
    suspend fun sendSensorHistory(@Body dto: List<OfflineDataRequest>)
}