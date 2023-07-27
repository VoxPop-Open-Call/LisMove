package it.lismove.app.android.authentication.repository

import it.lismove.app.room.entity.LisMoveCityEntity

interface CityRepository {
    fun getCities(): List<LisMoveCityEntity>
    fun getCity(id: Int): LisMoveCityEntity?
    fun getCity(name: String): LisMoveCityEntity?
}