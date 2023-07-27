package it.lismove.app.android.logWall.apiService

import retrofit2.http.GET

interface LogWallApi {
    @GET(" /logwall")
    suspend fun getLogWallItems(): List<String>
}