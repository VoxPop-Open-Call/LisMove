package it.lismove.app.android.authentication.ui.adapter

import it.lismove.app.android.authentication.ui.data.CityItemUI

interface CityAdapterCallback {
    fun onCityClicked(city: CityItemUI)
}