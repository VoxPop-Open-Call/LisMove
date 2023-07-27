package it.lismove.app.android.maps.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient

interface MapsRepository {
    suspend fun getLatLngFromAddress(client: PlacesClient, addressName: String): LatLng?
}