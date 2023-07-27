package it.lismove.app.android.initiative.ui

import android.content.Intent
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import it.lismove.app.android.authentication.repository.CityRepository
import it.lismove.app.android.authentication.ui.CityPickerActivity
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.android.maps.repository.MapsRepository
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import lv.chi.photopicker.utils.SingleLiveEvent
import timber.log.Timber
import java.lang.Error

class AddressPointAdjusterViewModel(
    private val repository: MapsRepository,
    private val cityRepository: CityRepository,
    private val user: LisMoveUser
): ViewModel() {

    lateinit var workAddress: WorkAddress
    var isHomeAddress: Boolean = false
    var latLng: MutableStateFlow<Lce<LatLng?>> = MutableStateFlow(LceSuccess(null))
    var latLngObservable = latLng.asLiveData()
    var showAlertEvent = SingleLiveEvent<Boolean>()

    fun setWorkAddress(intent: Intent){
        val addressString = intent.getStringExtra(BasicPointAdjusterActivity.INTENT_ADDRESS)
        workAddress = Gson().fromJson(addressString, WorkAddress::class.java)
        isHomeAddress = intent.getBooleanExtra(BasicPointAdjusterActivity.INTENT_IS_HOME_ADDRESS, false)
    }

    fun getLatLng(client: PlacesClient)  {
        viewModelScope.launch {
            try{
                with(latLng){
                    emit(LceLoading())
                    if(workAddress.lat != null && workAddress.lng != null){
                        emit(LceSuccess(LatLng(workAddress.lat!!, workAddress.lng!!)))
                    }else if(workAddress.completeName.isEmpty().not()){
                        val latLng = repository.getLatLngFromAddress(client, workAddress.completeName)
                        if(latLng != null){
                            emit(LceSuccess(latLng))
                        }else{
                            emit(LceError(Error("Lat Long is null")))
                        }
                    }
                }

            } catch (e: Exception){
                latLng.emit(LceError(e))
            }
        }

    }

    fun fetchLatLngFromWorkAddressComponents(client: PlacesClient){
        viewModelScope.launch {
            with(latLng){
                emit(LceLoading())
                val latLng = repository.getLatLngFromAddress(client, workAddress.completeName)
                workAddress.lat = latLng?.latitude
                workAddress.lng = latLng?.longitude
                if(latLng != null){
                    emit(LceSuccess(latLng))
                }else{
                    emit(LceError(Error("Si è verificato un errore nel recuperare la posizione")))
                }
            }

        }
    }

    fun updateAddress(place: Place){
        viewModelScope.launch {
            try {
                place.addressComponents?.asList()?.let {
                    latLng.emit(LceLoading())

                    val addressString = it.firstOrNull { it.types.map { it.lowercase() }.any { it == Place.Type.STREET_ADDRESS.name.lowercase() || it == Place.Type.ROUTE.name.lowercase() }}?.name
                    val number = it.firstOrNull { it.types.map { it.lowercase() }.contains(Place.Type.STREET_NUMBER.name.lowercase()) }?.name
                    val city = it.firstOrNull {it.types.map { it.lowercase() }.any{it == Place.Type.LOCALITY.name.lowercase() || it == Place.Type.ADMINISTRATIVE_AREA_LEVEL_3.name.lowercase()} }?.name
                    val cityExtended = if(city != null )cityRepository.getCity(city) else null

                    if(addressString.isNullOrEmpty()  || city.isNullOrEmpty()){
                        latLng.emit(LceError(Exception("Inserisci un indirizzo completo")))
                    }else{
                        Timber.d("$addressString - $number  - $city - ${cityExtended?.getFullName()}")
                        cityExtended?.let {
                            workAddress.updateCity(it)
                        }
                        workAddress.address = addressString
                        workAddress.number = number
                        workAddress.lat = place.latLng?.latitude
                        workAddress.lng = place.latLng?.longitude
                        showAlertEvent.postValue(true)
                        latLng.emit(LceSuccess(place.latLng))
                    }

                }
            } catch (e: Exception) {
                latLng.emit(LceError(e))
            }
        }
    }

    fun updateAddressAndGetLocation(address: String?, number: String?, client: PlacesClient){
        updateAddressAndNumber(address, number)
        fetchLatLngFromWorkAddressComponents(client)
    }

    fun setCityFormIntent(intent: Intent?, client: PlacesClient){
        if ( intent?.extras?.getString(CityPickerActivity.EXTRA_CITY) != null){
            val cityString = intent.extras?.getString(CityPickerActivity.EXTRA_CITY)
            val city = Gson().fromJson(cityString, LisMoveCityEntity::class.java)
            workAddress.cityExtended = city
            workAddress.city = city.id
            fetchLatLngFromWorkAddressComponents(client)
        }
    }

    fun updateAddressAndNumber(address: String?, number: String?){
        workAddress.address = address
        workAddress.number = number
    }

    fun updatePosition(lat: Double, lng: Double){
        workAddress.lng = lng
        workAddress.lat = lat
    }

    fun ignoreNumber(): Boolean{
        return true
    }

}