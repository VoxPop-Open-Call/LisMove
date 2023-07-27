package it.lismove.app.android.car.apiService

import it.lismove.app.android.car.data.*
import retrofit2.http.GET
import retrofit2.http.Path

interface CarDataApi {
    @GET("/carbrands")
    suspend fun getBrands(): List<CarBrand>

    @GET("/carbrands/{bid}/models")
    suspend fun getModels(@Path("bid") bid: String): List<CarModel>

    @GET("/carbrands/{bid}/models/{mid}/generations")
    suspend fun getGenerations(@Path("bid") bid: String,
                               @Path("mid") mid: String): List<CarGeneration>

    @GET("/carbrands/{bid}/models/{mid}/generations/{gid}/modifications")
    suspend fun getModification(@Path("bid") bid: String,
                                @Path("mid") mid: String,
                                @Path("gid") gid: String): List<CarModification>
}