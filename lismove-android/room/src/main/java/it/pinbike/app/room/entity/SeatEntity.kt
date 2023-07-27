package it.lismove.app.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SeatEntity (
    val address: String,
    @Embedded var cityExtended: LisMoveCityEntity? = null,
    val city: Int?,
    val cityName: String?,
    @PrimaryKey
    val id: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val name: String?,
    val number: String?,
    val organization: Long,
    val validated: Boolean? = null
){
    fun isEqual(seatEntity: SeatEntity): Boolean{

        return seatEntity.address.contentEquals(address) && seatEntity.city == city &&
        seatEntity.name.contentEquals(name) && seatEntity.number.contentEquals(number) &&
        seatEntity.latitude == latitude && seatEntity.longitude == longitude
    }

    fun getAddressString(): String{
        return "$address $number, ${cityExtended?.getFullName()}"
    }
}
