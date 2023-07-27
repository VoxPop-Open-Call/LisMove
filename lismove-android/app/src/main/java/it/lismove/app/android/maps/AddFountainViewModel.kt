package it.lismove.app.android.maps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import it.lismove.app.android.maps.data.DrinkingFountain
import it.lismove.app.android.maps.data.FountainClusterItem
import it.lismove.app.android.maps.repository.DrinkingFountainRepository
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.LismoveSensorSdk
import org.joda.time.DateTimeUtils
import timber.log.Timber
import kotlin.Exception

class AddFountainViewModel(
    private val drinkingFountainRepository: DrinkingFountainRepository,
    private val sensorSdk: LismoveSensorSdk,
    private val user: LisMoveUser
    ): ViewModel() {
    var markerPosition: LatLng? = null
    var drinkingFountains: List<FountainClusterItem> = listOf()

    suspend fun getLastLocation(applicationContext: Context): LatLng? {
        return try{
            val location = sensorSdk.getLastLocation(applicationContext)
            if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        }catch (e: Exception){
            null
        }
    }

    fun changeMarkerPosition(latLng: LatLng){
        markerPosition = latLng
    }

    suspend fun saveDrinkingFountain(){
        delay(2000)
        viewModelScope.launch {
            if (markerPosition != null){
               if(drinkingFountains.contains(FountainClusterItem(markerPosition!!.latitude, markerPosition!!.longitude, null, null))){
                   throw Exception("Esiste già una fontanella nella posizione indicata")
               }
                addDrinkingFountain(markerPosition!!)
            }else{
                throw Exception("Inserisci una posizione")
            }
        }
    }

    private suspend fun addDrinkingFountain(latLng: LatLng){
        val newDrinkingFountain = DrinkingFountain(
            name = "",
            lat = latLng.latitude,
            lng = latLng.longitude,
            uid = user.uid,
            createdAt = DateTimeUtils.currentTimeMillis()
        )
        drinkingFountainRepository.addDrinkingFountain( newDrinkingFountain)
    }

    suspend fun fetchDrinkingFountains(): List<FountainClusterItem>{
        return try {
            drinkingFountains = drinkingFountainRepository.getDrinkingFountainList().map { FountainClusterItem(
                lat = it.lat,
                lng = it.lng,
                title = null,
                snippet = null
            ) }
            return drinkingFountains
        }catch (e: java.lang.Exception){
            Timber.d("Error ${e.localizedMessage}")
            listOf()
        }
    }
}