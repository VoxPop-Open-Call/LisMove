package it.lismove.app.android.authentication.repository

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import it.lismove.app.android.R
import it.lismove.app.room.entity.LisMoveCityEntity

class CityRepositoryImpl(
    val context: Context
): CityRepository {
    override fun getCities(): List<LisMoveCityEntity> {
        val inputStream = context.resources.openRawResource(R.raw.comuni)
        val cities = arrayListOf<LisMoveCityEntity>()
        csvReader().readAll(inputStream).forEach {
            val elem = it.first().split(";")
            val elementId = elem[0].toInt()
            cities.add(LisMoveCityEntity(elementId, elem[1], elem[2]))
        }
        return cities

    }

    override fun getCity(id: Int): LisMoveCityEntity? {
        return getCities().firstOrNull { it.id == id }
    }

    override fun getCity(name: String): LisMoveCityEntity? {
        return getCities().firstOrNull { it.name.lowercase() == name.lowercase() }
    }

}