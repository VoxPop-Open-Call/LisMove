package it.lismove.app.android.maps.data

import com.google.firebase.database.Exclude


data class DrinkingFountain(
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val uid: String? = null,
    var createdAt: Long? = null,
    var deleted: Boolean? = false,
    var deletedBy: String? = null,
    var deletedAt: Long? = null,


){
    @Exclude
    var id: String? = ""
}
