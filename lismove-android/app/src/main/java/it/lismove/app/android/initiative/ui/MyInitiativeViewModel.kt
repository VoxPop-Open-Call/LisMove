package it.lismove.app.android.initiative.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.initiative.ui.parser.asSimpleItem
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class MyInitiativeViewModel(
    val userRepository: UserRepository,
    var user: LisMoveUser
): ViewModel() {
    private val state: MutableStateFlow<Lce<List<SimpleItem>>> = MutableStateFlow(LceSuccess(listOf()))
    val stateObservable = state.asLiveData()
    var enrollments: List<EnrollmentWithOrganization> = listOf()

    fun loadInitiatives(){
        viewModelScope.launch {
            try {
                state.emit(LceLoading())
                enrollments = userRepository.getInitiatives(user.uid).sortedByDescending { it.enrollment.endDate }
                state.emit(LceSuccess(enrollments.map { it.asSimpleItem() }))
            }catch (e: Exception){
                state.emit(LceError(e))
            }
        }
    }

    fun getEnrollment(id: String): EnrollmentWithOrganization {
        return enrollments.first { it.enrollment.id == id.toLong() }
    }


}