package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.lismove.app.common.sanitizeHtmlText

@Entity
data class OrganizationEntity (
    val geojson: String?,
    @PrimaryKey
    val id: Long,
    val initiativeLogo: String?,
    val logo: String?,
    val notificationLogo: String?,
    val pageDescription: String?,
    val termsConditions: String?,
    val regulation: String?,
    val title: String,
    var type: Int,
    val validation: Boolean){

    fun getGeoJsonCoordinates(): List<List<LatLngResponse>>{
        val geoJsonType = object : TypeToken<List<List<LatLngResponse>>>() {}.type
        return  Gson().fromJson(geojson, geoJsonType)
    }

    fun getSanitizedRegulation() = regulation?.sanitizeHtmlText()

    companion object{
        val TYPE_PA= 0
        val TYPE_COMPANY = 1
    }
}
data class LatLngResponse(val lat: Double, val lng: Double)
