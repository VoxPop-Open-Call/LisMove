package it.lismove.app.android.maps

import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.maps.data.DrinkingFountain
import it.lismove.app.android.maps.data.FountainClusterItem
import it.lismove.app.android.maps.data.InitiativePolygon
import it.lismove.app.android.maps.parser.asInitiativePolygon
import it.lismove.app.android.maps.repository.DrinkingFountainRepository
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.LismoveSensorSdk
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.statusListener.GpsStatusListener
import timber.log.Timber
import java.lang.Exception

class MapViewModel(
        context: Context,
        private val sensorSdk: LismoveSensorSdk,
        private val sessionRepository: SessionSdkRepository,
        private val userRepository: UserRepository,
        private val drinkingFountainRepository: DrinkingFountainRepository,
        private val user: LisMoveUser
): ViewModel() {

    val errorObservable: MutableLiveData<String> = MutableLiveData()
    var showFountain: Boolean = true
    var showInitiativePolygon: Boolean = true

    var initiativePolygons: List<InitiativePolygon> = listOf()

    var drinkingFountainsUI: List<FountainClusterItem> = listOf()
    var drinkingFountains: List<DrinkingFountain> = listOf()

    private val pathFlow =  MutableStateFlow<List<LatLng>>(listOf())
    val pathLiveData = pathFlow.asLiveData()
    val gpsConnectivity: LiveData<Boolean>
    var sessionId: String? = null
    init {
        gpsConnectivity = GpsStatusListener(context)

    }


    fun setSessionFromIntent(intent: Intent){
        sessionId = intent.getStringExtra(MapsActivity.INTENT_SESSION_ID)
        sessionId?.let {
            fetchPositions(it)

        }
    }
    suspend fun getInitiativePolygon(): List<InitiativePolygon> {
        return try{
            val initiatives = userRepository.getActiveInitiatives(user.uid).filter { it.organization.geojson != null }
            initiativePolygons = initiatives.map { it.asInitiativePolygon()}
            return initiativePolygons
        }catch (e: Exception){
            errorObservable.postValue(e.localizedMessage)
            listOf()
        }
    }

    suspend fun fetchDrinkingFountains(): List<FountainClusterItem>{
        return try {
            drinkingFountains = drinkingFountainRepository.getDrinkingFountainList()
            drinkingFountainsUI = drinkingFountains.map {
                FountainClusterItem(
                    lat = it.lat,
                    lng = it.lng,
                    title = null,
                    snippet = null
                )
            }
            return drinkingFountainsUI
        }catch (e: Exception){
            Timber.d("Error ${e.localizedMessage}")
            listOf()
        }
    }

    private fun fetchPositions(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.getPartialsObservable(sessionId).collect {
                val coordinats = ArrayList<LatLng>()

                it.forEach { partial ->
                    Timber.d("${partial.latitude} - ${partial.longitude}")

                    if(partial.latitude != 0.0 && partial.longitude != 0.0){
                        if (partial.latitude != null && partial.longitude != null){
                            coordinats.add(LatLng(partial.latitude ?: 0.0, partial.longitude ?: 0.0))
                        }
                    }
                }
                Timber.d("end ${coordinats.size}")
                pathFlow.emit(coordinats)
            }

        }
    }

    suspend fun getLastLocation(applicationContext: Context): LatLng? {
        try{
            val location = sensorSdk.getLastLocation(applicationContext)
            if (location != null) {
                return LatLng(location.latitude, location.longitude)
            } else {
                return null
            }
        }catch (e: Exception){
            return null
        }
    }

    suspend fun  deleteFountain(position: LatLng): FountainClusterItem {
        drinkingFountains.firstOrNull { it.lat == position.latitude && it.lng == position.longitude }?.let { drinkingFountain ->
            if(drinkingFountain.deleted != true){
                drinkingFountainRepository.deleteDrinkingFountain(drinkingFountain, user.uid)
                val removedDF = drinkingFountainsUI.first { it.position == position }
                drinkingFountains = drinkingFountains.filterNot { it.lat == position.latitude && it.lng == position.longitude  }
                drinkingFountainsUI = drinkingFountainsUI.filterNot { it.position == position }
                return removedDF
            }
            throw Exception("Fontanella non trovata")
        }
        throw Exception("Fontanella non trovata")
    }

}




