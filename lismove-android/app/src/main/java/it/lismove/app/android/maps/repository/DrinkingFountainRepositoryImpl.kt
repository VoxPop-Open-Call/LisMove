package it.lismove.app.android.maps.repository

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import it.lismove.app.android.general.network.NetworkConfig.FIREBASE_RTDB_URL
import it.lismove.app.android.maps.data.DrinkingFountain
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.joda.time.DateTimeUtils
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DrinkingFountainRepositoryImpl: DrinkingFountainRepository {

    override suspend fun getDrinkingFountainList(): List<DrinkingFountain>  {
        val database = Firebase.database(FIREBASE_RTDB_URL)
        val res = database.reference.child("drinkingFountains").get().await()
        val fountains = res.children.map { it.getValue(DrinkingFountain::class.java).apply {
            this?.id = it.key
        } }.filterNotNull().filter { it.deleted != true }
        Timber.d("result is ${fountains.size}")
        return fountains
    }

    override suspend fun addDrinkingFountain(fountain: DrinkingFountain) {
        Timber.d(FIREBASE_RTDB_URL)
        val database = Firebase.database(FIREBASE_RTDB_URL)
        database.reference.child("drinkingFountains")
            .child(UUID.randomUUID().toString())
            .setValue(fountain)
            .await()



    }

    override suspend fun deleteDrinkingFountain(fountain: DrinkingFountain, uid: String) {
        fountain.deleted = true
        fountain.deletedBy = uid
        fountain.deletedAt = DateTimeUtils.currentTimeMillis()
        if(fountain.id == null){
            throw Exception("Fountain id is null")
        }else {
            Timber.d("ok")
            val database = Firebase.database(FIREBASE_RTDB_URL)
            database.reference.child("drinkingFountains")
                .child(fountain.id!!)
                .setValue(fountain)
                .await()
        }

    }


}
