package it.lismove.app.android.awards

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.R
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.awards.data.Award
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.awards.parser.asAwardItemUI
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AwardWrapperViewModel(
    val userRepository: UserRepository,
    val user: LisMoveUser
): ViewModel() {
    var awards: List<Award> = listOf()
    private val awardFlow = MutableStateFlow<Lce<AwardsUI>> (LceSuccess(AwardsUI("", "", listOf())))
    val awardObservable = awardFlow.asLiveData()
    var myAwardsTitle = "I miei premi"
    var enablePicker = true

    fun getData(intent: Intent){

        val type = intent.getStringExtra(AwardWrapperActivity.TYPE)
        if(type.isNullOrEmpty().not()){
            enablePicker = true
            if(type == AwardWrapperActivity.TYPE_MY_AWARD){
                loadMyAwardsLce()
            }
        }else{
            loadMyAwardsLce()
        }

    }

    private fun loadMyAwardsLce(){
        viewModelScope.launch {
            awardFlow.emit(LceLoading())
            delay(3000)
            try {
                myAwardsTitle = "I miei premi"
                val awardsUI = getMyAwards()
                //val awardsUI = getMockedAwardItemUI()
                awardFlow.emit(LceSuccess(AwardsUI(myAwardsTitle, "", awardsUI )))
            }catch (e: Exception){
                awardFlow.emit(LceError(e))
            }
        }
    }
    private suspend fun getMyAwards(): List<AwardItemUI> {
        awards =  userRepository.getAwards(user.uid).sortedWith(compareByDescending<Award> {it.refundOrderValue }.thenByDescending { it.timestamp})
        return awards.mapIndexed { index, award ->
            award.asAwardItemUI(index)
        }
    }

    fun getAwardFromIndex(index: Int): Award{
        return awards[index]
    }

    private suspend fun getMockedAwardItemUI(): List<AwardItemUI> {
        return listOf(
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Clssifica mensile",
                R.drawable.ic_ticket_done,
                "RIMBORSATO",
                R.color.gray_image_tint,
                null,
                "5",
            "euro"
            ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Clssifica mensile",
                R.drawable.ic_ticket_base,
                "DA\n RIMBORSARE",
                R.color.red_main,
                "",
                "5",
            "euro"
            ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Primo Premio",
                R.drawable.ic_ticket_base,
                "DA\nRISCATTARE",
                R.color.red_main,
                null,
                ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Primo Premio",
                R.drawable.ic_ticket_done,
                "RISCATTATO",
                R.color.gray_image_tint,
                null,
                null,
                null,
            ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Primo Premio",
                null,
                null,
                R.color.red_main,
                null,
                "20",
                "punti",
            ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Buono 20%",
                R.drawable.ic_ticket_base,
                "DA\nRISCATTARE",
                R.color.red_main,
                null,
                "123",
                "punti",
            ),
            AwardItemUI(
                "123",
                "https://www.reportec.it/wp-content/uploads/2021/03/veeam-award-300x225.jpg",
                "Buono 20%",
                R.drawable.ic_ticket_done,
                "RISCATTATO",
                R.color.gray_image_tint,
                null,
                "123",
                "punti",
            ),
        )
    }

}

