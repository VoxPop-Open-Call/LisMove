package it.lismove.app.android.gaming.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.R
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.repository.OrganizationRepository
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.gaming.apiService.data.LismoverRank
import it.lismove.app.android.gaming.apiService.data.Ranking
import it.lismove.app.android.gaming.repository.RankingRepository
import it.lismove.app.android.gaming.ui.data.RankingItemUI
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.lang.Exception

class RankingViewModel(
    val rankingRepository: RankingRepository,
    val organizationRepository: OrganizationRepository,
    val user: LisMoveUser
): ViewModel() {

    private var userRankings: List<Ranking> = listOf()
    private var rankingStatus: MutableStateFlow<Lce<RankingDetailUI>> = MutableStateFlow(LceLoading())
    var rankingObservable = rankingStatus.asLiveData()

    private lateinit var mainRanking: Ranking

    var selectedRanking: Ranking? = null


    fun loadData(){
        viewModelScope.launch {
            try{
                rankingStatus.emit(LceLoading())
                mainRanking = rankingRepository.getMainRanking().copy(title = "Community").copy(priority = 1)
                userRankings = rankingRepository.getUserRankings(user.uid)
                    .sortedWith(compareBy({ it.priority }, {it.startDate}, {it.endDate})).reversed()

                userRankings.forEach {
                    it.organization?.let {  oid ->
                        val organization = organizationRepository.getOrganization(oid)
                        it.organizationName = organization.title
                        it.organizationNotificationImage = organization.notificationLogo
                        it.organizaitonRegulation = organization.getSanitizedRegulation()
                    }
                }

                if(userRankings.isNullOrEmpty().not()){
                    setCurrentRanking(userRankings.first())
                }else{
                    setCurrentRanking(mainRanking)

                }

                userRankings = userRankings.toMutableList().apply {
                    add(0, mainRanking)
                }

            }catch (e: Exception){
                Timber.d("ERROR ${e.message}")
                rankingStatus.emit(LceError(e))
            }
        }

    }

    fun getRankings(): List<ListAlertData>{
        return userRankings.map {
            val imageRes = if(it.organizationNotificationImage.isNullOrEmpty()) R.drawable.ic_notification_session else null
            ListAlertData(it.id.toString(),
                it.title,
                "",
                it.organizationName,
                it.organizationNotificationImage,
                imageRes,
                bottomText = it.getDateIntervalLabel())
        }
    }

    fun changeRanking(item: ListAlertData){
        val ranking = userRankings.first{ it.id.toString() == item.id}
        setCurrentRanking(ranking)
    }


    private fun  setCurrentRanking(ranking: Ranking){
        viewModelScope.launch {
            try {
                rankingStatus.emit(LceLoading())

                if(mainRanking.id == ranking.id){
                    //Is main ranking
                    rankingStatus.emit(LceSuccess(mainRanking.asRankingDetailUI(user.username!!)))
                    return@launch
                }
                //Is initiative ranking

                val completeRanking = rankingRepository.getRanking(ranking.id).asRankingDetailUI(user.username!!)
                completeRanking.organizationTitle = "${ranking.organizationName}"
                completeRanking.hasPrize = true
                completeRanking.id = ranking.id
                selectedRanking = ranking
                rankingStatus.emit(LceSuccess(completeRanking))
            }catch (e: Exception){
                rankingStatus.emit(LceError(e))
            } catch (e: IOException) {
                rankingStatus.emit(LceError(Exception(e.message ?: "Nessuna connessione ad internet")))
            }
        }

    }


}

data class RankingDetailUI(
    var id: Long?,
    var organizationTitle: String?,
    val rankingTitle: String,
    val userPlacement: RankingItemUI?,
    val ranking: List<RankingItemUI>,
    val dayTillEnd: String?,
    var hasPrize: Boolean,
)

fun Ranking.asRankingDetailUI(username: String, isCommunity: Boolean = false): RankingDetailUI{
    val userPosition = rankingPositions?.firstOrNull { it.username ==  username}
    val days =  if(endDate != null) DateTimeUtils.daysUntil(endDate) else null
    val daysLabelIfNotEnded = if(days != null && days<= 0) "$days" else null
    val dateLabel = this.getDateIntervalLabel()
    val title2 = if(isCommunity || dateLabel == null) title else "$title\n$dateLabel"
    return RankingDetailUI(
        null,
        null,
        title,
        userPosition?.asRankingItemUI(getValueLabel(), userPosition.position),
        ranking = rankingPositions?.map { it.asRankingItemUI(getValueLabel(), userPosition?.position) } ?: listOf(),
        dayTillEnd = daysLabelIfNotEnded,
        hasPrize = false,
    )
}


fun LismoverRank.asRankingItemUI(label: String, userPosition: Int?):RankingItemUI{
    val backgroundColor = if(position == userPosition && position != null) R.color.userPositionBackground else R.color.white
    val positionLabel = position.asRankingPositionLabel()
  return RankingItemUI(
      username = username ?: "",
      picProfile = avatarUrl,
      points = points.toInt().toString(),
      position = position.toString(),
      pointsLabel = label,
      positionLabel = positionLabel,
      background = backgroundColor
  )
}

fun Int.asRankingPositionLabel(): String{
    if(this == 0) return ""
    else if(this == 11) return "TH"
    else if(this == 12) return "TH"
    else if(this == 13) return "TH"

    val lastDigit = this % 10
    return  when(lastDigit){
        0 -> "TH"
        1 -> "ST"
        2 -> "ND"
        3 -> "RD"
        else -> "TH"
    }

}