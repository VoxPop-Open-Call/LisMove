package it.lismove.app.android.gaming.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.gaming.ui.parser.asActiveAwardsSimpleItem
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ActiveAwardsViewModel(
    val userRepository: UserRepository,
    val user: LisMoveUser
): ViewModel() {
    private var initiatives: List<EnrollmentWithOrganization> = listOf()
    private var state = MutableStateFlow<Lce<List<SimpleItem>>>(LceSuccess(listOf()))
    var stateObservable = state.asLiveData()
    fun getState(){
        viewModelScope.launch {
            try {
                fetchActiveInitiatives()
            }catch (e: Exception){
                state.emit(LceError(e))
            }
        }
    }

    private suspend fun fetchActiveInitiatives() {
        state.emit(LceLoading())
        initiatives = userRepository.getActiveInitiatives(user.uid)
        state.emit(LceSuccess(initiatives.map { it.asActiveAwardsSimpleItem() }))
    }

    fun getInitiativeFromItem(item: SimpleItem): EnrollmentWithOrganization{
        return initiatives.first{it.organization.id.toString() == item.id}
    }
}