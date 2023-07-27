package it.lismove.app.android.session.ui

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import it.lismove.app.android.dashboard.data.RankingPointData
import it.lismove.app.android.dashboard.useCases.TotalPointsUseCase
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.utils.GpxUtils
import it.lismove.app.android.general.utils.toJson
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.ui.data.SessionDetailUI
import it.lismove.app.android.session.ui.parser.SessionDetailParser
import it.lismove.app.android.session.ui.useCase.SessionDetailUseCase
import it.lismove.app.common.ShareUtils
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SessionDetailViewModel(
    val totalPointsUseCase: TotalPointsUseCase,
    val sessionDetailUseCase: SessionDetailUseCase,
    val user: LisMoveUser
) : ViewModel(){

    var sessionDetail: SessionDetailUI? = null
    var session: Session? = null
    private val sessionDetailFlow: MutableStateFlow<Lce<SessionDetailUI>> = MutableStateFlow(LceLoading())
    val sessionDetailObservable = sessionDetailFlow.asLiveData()
    var isActivityVisible = false
    var isFromHistory = false
    var validationInProgress = false

    lateinit var sessionId: String

    fun setData(id: String, mIsFromHistory: Boolean){
        isFromHistory = mIsFromHistory
        sessionId = id

    }
    fun loadSessionData() {

        viewModelScope.launch(Dispatchers.IO) {
            with(sessionDetailFlow){
                Timber.d("loadSessionData")
                try {
                    emit(LceLoading())
                    val fSession = fetchSession(sessionId)
                    updateSession(fSession)
                    if(fSession.valid != null  && fSession.uploaded && validationInProgress.not()) {
                        launchCheckValidationUpdateLoop()
                    }
                    emit(LceSuccess(sessionDetail!!))
                }catch (e: Exception){
                    emit(LceError(e))
                }
            }
        }
    }

    fun reloadSessionData(){

        viewModelScope.launch(Dispatchers.IO) {
            with(sessionDetailFlow){
                try {
                    emit(LceLoading())
                    val fSession = fetchSession(sessionId)
                    updateSession(fSession)
                    emit(LceSuccess(sessionDetail!!))
                }catch (e: Exception){
                    emit(LceError(e))
                }
            }
        }
    }

    private suspend fun getSessionDetailUIFromSession(session: Session): SessionDetailUI{
        var totalPointsNational: RankingPointData? = null
        var totalPointsInitiative: List<RankingPointData>? = null

        if(isFromHistory.not()){
            totalPointsNational = totalPointsUseCase.getTotalNationalPoints(user)
            totalPointsInitiative = totalPointsUseCase.getTotalActiveInitiativePoints(user)
        }

        sessionDetail = SessionDetailParser.getSessionDetail(
            session,
            totalPointsNational,
            totalPointsInitiative,
            isFromHistory,
        )

        return sessionDetail!!
    }

    private fun launchCheckValidationUpdateLoop(){
        viewModelScope.launch(Dispatchers.IO) {
            while (validationInProgress){
                delay(20000)
                if(isActivityVisible){
                    val session = fetchSession(sessionId)
                    if(session.valid != null){
                        sessionDetail = getSessionDetailUIFromSession(session)
                        sessionDetail?.let {
                            sessionDetailFlow.emit(LceSuccess(it))
                        }
                    }
                    validationInProgress = session.valid == null
                    delay(20000)

                }
            }
        }
    }

    private suspend fun fetchSession(id: String): Session{
        val fsession = sessionDetailUseCase.getSession(id, user.uid)
        session = fsession
        sessionId = fsession.id!!
        return fsession
    }

    fun getSessionShareString(): String{
        var text = ""
        sessionDetail?.let {
            text = "Ho concluso una sessione in bici con Lismove di ${it.distance} km in ${it.duration}" +
            " e ho guadagnato ${it.sessionPointsNational} punti Community e ${it.sessionInitiativeNumber} punti Iniziativa.\n"
        }
        return text
    }

    fun shareGpx(sessionId: String, ctx: Context) = viewModelScope.launch {
        try {
            val sessionWithPartials = sessionDetailUseCase.getSessionWithPartials(sessionId)
            GpxUtils.buildGpxAndShare(sessionWithPartials, ctx)
        }catch (e: Exception){
            Toast.makeText(ctx, "Impossibile generare file Gpx al momento. Riprova più tardi.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getSessionPoints(): LiveData<Lce<List<ListAlertData>>> = liveData<Lce<List<ListAlertData>>>{

        emit(LceLoading())
        var res = sessionDetailUseCase.getSessionPoints(sessionId, user.uid)
        if(res.isEmpty()) res = listOf(ListAlertData(null, "Nessun progetto attivo", ""))
        emit(LceSuccess(res))
    }

    fun getTotalPointsItem(totalPointsInitiative: List<RankingPointData>): List<ListAlertData>{
        if(totalPointsInitiative.isEmpty()){
            return listOf(ListAlertData(null, "Nessun progetto attivo", ""))
        }
        return totalPointsInitiative.map {ListAlertData(it.organizationId?.toString(), it.activeInitiative, it.points.toString()) }
    }

    suspend fun requestSessionVerification(reason: String){
        if(session == null) throw Exception()
        withContext(Dispatchers.IO){
            updateSession(sessionDetailUseCase.requestPointVerification(sessionId, user.uid, reason))
        }
    }

    //TODO: Change this with new email
    fun shareSessionManually(activity: AppCompatActivity) {
        val serializedSession = session?.toJson() ?: "Session was not available"

        ShareUtils.saveAttachmentAndOpenShareIntent(
            title = "[LisMove Beta Session] ${user.username} - ${session?.startTime}",
            receiverEmail = "p.rotolo@nextome.net",
            attachmentText = serializedSession,
            fileName = "session_${session?.startTime}",
            activity = activity
        )
    }

    private suspend fun updateSession(newSession: Session){
        session = newSession
        sessionDetail = getSessionDetailUIFromSession(newSession)
        sessionDetailFlow.emit(LceSuccess(sessionDetail!!))
    }
}