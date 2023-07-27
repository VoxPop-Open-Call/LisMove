package it.lismove.app.android.car.repository

import it.lismove.app.android.car.data.*

interface CarRepository {
    suspend fun getBrands(): List<CarBrand>
    suspend fun getModels(bid: String): List<CarModel>
    suspend fun getGenerations(bid: String, mid: String): List<CarGeneration>
    suspend fun getModifications(bid: String, mid: String, gid: String): List<CarModification>
}