package it.lismove.app.android.maps.repository

import it.lismove.app.android.maps.data.DrinkingFountain

interface DrinkingFountainRepository {
     suspend fun getDrinkingFountainList(): List<DrinkingFountain>
     suspend fun addDrinkingFountain(fountain: DrinkingFountain)
     suspend fun deleteDrinkingFountain(fountain: DrinkingFountain, uid: String)
}