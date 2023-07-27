package it.lismove.app.android.session.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.ui.data.SessionListItemUI
import it.lismove.app.android.session.repository.ApplicationSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class SessionsHistoryViewModel(
        val sessionRepository: ApplicationSessionRepository,
        val user: LisMoveUser
): ViewModel() {
    private val _state = MutableStateFlow<Lce<List<SessionListItemUI>>>(LceLoading())
    val state = _state.asLiveData()

    var endDate: Date = DateTime().withTimeAtStartOfDay().toDate()
    var startDate: Date = DateTime().minusMonths(1).toDate()
    private var filterValues = MutableStateFlow<String>(getFilterValueString())
    val filterValueObservable  = filterValues.asLiveData()
    var onlyWorkPath: Boolean = false


    fun getSessionData(onlyWorkPath: Boolean){
        this.onlyWorkPath = onlyWorkPath
        viewModelScope.launch {
            _state.emit(LceLoading())
            val sessions = sessionRepository.getSessionList(user.uid, startDate, endDate).filter { onlyWorkPath.not() || it.homeWorkPath == true  }.map {
                    session ->
                session.asSessionListItemUI()
            }
            _state.emit(LceSuccess(sessions))

        }
    }

    fun getFilterValueString(): String{
        val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val startDateString =  formatter.format(startDate)
        val endDateString =  formatter.format(endDate)
        return "$startDateString - $endDateString"
    }

    fun getDateFromResultIntent(intent: Intent?){
        val startDateString = intent?.getStringExtra(FilterDatePickerActivity.INTENT_START_DATE)
        val endDateString = intent?.getStringExtra(FilterDatePickerActivity.INTENT_END_DATE)
        startDateString?.let {
            startDate = Gson().fromJson(it, Date::class.java)
        }
        endDateString?.let {
            endDate = Gson().fromJson(it, Date::class.java)

        }
        viewModelScope.launch {
            filterValues.emit(getFilterValueString())
        }
        getSessionData(onlyWorkPath)
        Timber.d("data received")
    }
}

fun Session.asSessionListItemUI(): SessionListItemUI {
    return SessionListItemUI(
            id = this.id ?: "",
            distance = "${this.getDistanceReadable()} Km ",
            nationalPoint = this.getValidatedNationalPoints().toString(),
            initiativePoint = this.getValidatedTotalInitiativePoints().toString(),
            date = DateTimeUtils.getReadableDateTime(this.startTime),
            showRoundAlert = this.uploaded.not()
    )
}