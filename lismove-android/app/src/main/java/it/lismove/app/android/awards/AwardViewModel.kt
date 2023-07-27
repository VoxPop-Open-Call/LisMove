package it.lismove.app.android.awards

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.R
import it.lismove.app.android.awards.data.AwardAchievement
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.awards.data.AwardRanking
import it.lismove.app.android.awards.repository.AwardRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class AwardViewModel(
    val awardRepository: AwardRepository
): ViewModel() {

    var callerId: Long = 0
    var title: String? = null
    var subtitle: String? = null
    var type: String = ""
    private val awardFlow = MutableStateFlow<Lce<AwardsUI>> (LceSuccess(AwardsUI("", "", listOf())))
    val awardObservable = awardFlow.asLiveData()
    var rankingAwards: List<AwardRanking> = listOf()
    var achievementAwards: List<AwardAchievement> = listOf()

    fun initFromIntent(intent: Intent){
        val intentType = intent.getStringExtra(AwardActivity.INTENT_TYPE)
        requireNotNull(intentType)
        type = intentType
        callerId = intent.getLongExtra(AwardActivity.INTENT_CALLER_ID, 0)
        title = intent.getStringExtra(AwardActivity.INTENT_TITLE)
        subtitle = intent.getStringExtra(AwardActivity.INTENT_SUBTITLE)
    }

    fun getData(){
        viewModelScope.launch {
            try {
                awardFlow.emit(LceLoading())
                val awardsUI = getAwardItemUI()
                awardFlow.emit(LceSuccess(AwardsUI(title, subtitle, awardsUI)))
            }catch (e: Exception){
                awardFlow.emit(LceError(e))
            }

        }
    }

    private suspend fun getAwardItemUI(): List<AwardItemUI>{
        return when (type) {
            AwardActivity.INTENT_TYPE_RANKING -> {
                getRankingAwards()
            }
            AwardActivity.INTENT_TYPE_ACHIEVEMENT -> {
                getAchievementAwards()
            }
            else -> {
                throw Exception("Error with type")
            }
        }
    }
    private suspend fun getRankingAwards(): List<AwardItemUI>{
        rankingAwards =  awardRepository.getAwardsByRankingId(callerId)
        return  rankingAwards.sortedBy { it.position }.map {
            AwardItemUI(
                id = it.id.toString(),
                image = it.imageUrl,
                name = it.name,
                value = it.value.toString(),
                valueType = it.getTypeLabel()
            )
        }
    }

    private suspend fun getAchievementAwards(): List<AwardItemUI>{
        achievementAwards = awardRepository.getAwardsByAchievementId(callerId)
        return achievementAwards.map {
            AwardItemUI(
                id = it.id.toString(),
                image = it.imageUrl,
                name = it.name,
                value = it.value.toString(),
                valueType = it.getTypeLabel()
            ) }
    }

    fun getRankingAward(id: String): AwardRanking{
       return rankingAwards.first { it.id.toString() == id }
    }

    fun getAchievementAward(id: String): AwardAchievement{
        return achievementAwards.first { it.id.toString() == id }
    }


}


data class AwardsUI(
    val title: String?,
    val subtitle: String?,
    val items: List<AwardItemUI>
)