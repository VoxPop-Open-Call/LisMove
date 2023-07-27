package it.lismove.app.android.authentication.ui

import androidx.lifecycle.*
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.android.authentication.repository.CityRepository
import it.lismove.app.android.authentication.ui.data.CityItemUI
import kotlinx.coroutines.launch

class CityPickerViewModel(
    private val cityRepository: CityRepository
): ViewModel() {
    private val _filteredCities = MutableLiveData<List<CityItemUI>>()
    private var citiesUi: List<CityItemUI> = listOf()
    private var cities: List<LisMoveCityEntity> = listOf()

    fun fetchCities(): LiveData<List<CityItemUI>>{
        viewModelScope.launch {
            cities = cityRepository.getCities()
            citiesUi = cities.map { CityItemUI(it.id, "${it.name}, ${it.province}", false) }
            _filteredCities.value = (citiesUi)
        }
        return _filteredCities
    }

    fun filterCities(filter: String){
        _filteredCities.postValue(citiesUi.filter { it.name.toLowerCase().contains(filter.toLowerCase()) })
    }

    fun getCityFromId(id: Int): LisMoveCityEntity {
        return cities.first { it.id == id }
    }
}
