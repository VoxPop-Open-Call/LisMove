package net.nextome.lismove_sdk.utils

import com.google.android.gms.maps.model.LatLng
import it.lismove.app.room.entity.LatLngResponse

fun List<List<LatLngResponse>>.asLatLngLists(): List<List<LatLng>>{
    return this.map { it.map { LatLng(it.lat, it.lng) } }
}