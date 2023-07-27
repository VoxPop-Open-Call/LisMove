package it.lismove.app.android.car.repository

import it.lismove.app.android.car.apiService.CarDataApi
import it.lismove.app.android.car.data.*

class CarRepositoryImpl(
    val carDataApi: CarDataApi
): CarRepository {
    override suspend fun getBrands(): List<CarBrand> {
        return carDataApi.getBrands()
    }

    override suspend fun getModels(bid: String): List<CarModel> {
        return  carDataApi.getModels(bid)
    }

    override suspend fun getGenerations(bid: String, mid: String): List<CarGeneration> {
        return carDataApi.getGenerations(bid, mid)
    }

    override suspend fun getModifications(
        bid: String,
        mid: String,
        gid: String
    ): List<CarModification> {
        return carDataApi.getModification(bid, mid, gid)
    }
}