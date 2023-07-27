package it.lismove.app.android.maps.repository

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import it.lismove.app.android.R
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MapsRepositoryImpl: MapsRepository {

    override suspend fun getLatLngFromAddress(client: PlacesClient, addressName: String): LatLng? {
        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setCountries("IT")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(addressName)
                .build()

        client.findAutocompletePredictions(request).await().autocompletePredictions.firstOrNull()?.let {
            return getPlaceDetailSuspend(it.placeId, client).latLng
        }

        return null

    }
    suspend fun getPlaceDetailSuspend(placeId: String, placesClient: PlacesClient): Place{
        Timber.d("placedetail")
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val response = placesClient.fetchPlace(request).await()
        return response.place

    }


}