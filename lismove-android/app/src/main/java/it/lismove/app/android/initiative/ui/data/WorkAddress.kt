package it.lismove.app.android.initiative.ui.data

import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.SeatEntity
import org.joda.time.DateTimeUtils

data class WorkAddress (
    var id: Long = System.currentTimeMillis(),
    var editable: Boolean = true,
    var deletable: Boolean = false,
    var showName: Boolean = false,
    var name: String? = null,
    var address: String? = null,
    var number: String? = null,
    var city: Int? = null,
    var cityExtended: LisMoveCityEntity? = null,
    var lat: Double? = null,
    var lng: Double? = null
){
    val errorString = "Inserisci un valore valido"

    constructor(seatEntity: SeatEntity): this(seatEntity.id?.toLong() ?: DateTimeUtils.currentTimeMillis(),
        true,false, true, seatEntity.name, seatEntity.address, seatEntity.number,
        seatEntity.city, seatEntity.cityExtended, seatEntity.latitude, seatEntity.longitude)

    fun isNameValid(): Boolean{
        if(showName){
            return !name.isNullOrEmpty()
        }else{
            return true
        }
    }
    fun isComplete(ignoreNumber: Boolean = false): Boolean{
        return city != null && !address.isNullOrEmpty() && (!number.isNullOrEmpty() || ignoreNumber)
    }

    fun getAddressError(): String? {
        return if(address.isNullOrEmpty()) errorString else null
    }
    fun getNumberError(): String? {
        return if(number.isNullOrEmpty()) errorString else null
    }
    fun getCityError(): String? {
        return if(cityExtended == null) errorString else null
    }
    fun getNameError(): String?{
        return if(!isNameValid()) errorString else null
    }
    fun getFullAddressError(): String?{
        return if(isComplete()) null else "Inserisci tutti i dati richiesti"
    }
    fun updateCity(cityExtended: LisMoveCityEntity){
        this.cityExtended = cityExtended
        this.city = cityExtended.id
    }

    var completeName: String = ""
        get() {
            if(address.isNullOrEmpty() && number.isNullOrEmpty() && cityExtended == null){
                return ""
            }
            val addressString = "${address ?: ""} ${number ?: ""}, ${cityExtended?.getFullName() ?: ""}"
            return addressString.trim()
        }
}