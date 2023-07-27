package it.lismove.app.android.initiative.ui

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.initiative.ui.data.AddressItemUI
import it.lismove.app.android.initiative.ui.parser.asAddressItemUI
import it.lismove.app.room.entity.LisMoveCityEntity
import it.lismove.app.room.entity.SeatEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class CompanySeatPickerViewModel(
    private val organizationRepository: OrganizationRepository
): ViewModel() {

    private var organizationId: Long? = null

    private var seats: List<SeatEntity> = listOf()
    private var filteredSeats: List<SeatEntity> = seats

    private var seatsFlow: MutableStateFlow<Lce<List<AddressItemUI>>> = MutableStateFlow(LceLoading())
    val seatsObservable = seatsFlow.asLiveData()

    fun getSeats(intent: Intent) {
        viewModelScope.launch {
            try {
                organizationId = intent.getLongExtra(CompanySeatPickerActivity.ORGANIZATION_ID, 0)
                seats = organizationRepository.getSeats(organizationId!!)
                //seats = getSeatMocked()
                filteredSeats = seats
                seatsFlow.emit(LceSuccess(filteredSeats.map { it.asAddressItemUI()  }))
            } catch (e: Exception) {
                seatsFlow.emit(LceError(e))
            }
        }
    }



    fun getSeatEntity(id: Long): SeatEntity{
        return seats.first { it.id == id }
    }

    fun filterSeats(filter: String?){
        filteredSeats = if(filter.isNullOrEmpty()){
            seats
        }else{
            seats.filter {
                val name = it.name ?: ""
                name.lowercase(Locale.getDefault()).contains(filter.lowercase(Locale.getDefault())) ||
                    it.getAddressString().lowercase(Locale.getDefault()).contains(filter.lowercase(
                        Locale.getDefault()
                    )) }
        }
        viewModelScope.launch {
            seatsFlow.emit(LceSuccess(filteredSeats.map { it.asAddressItemUI()}))
        }
    }

}

