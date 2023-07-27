package it.lismove.app.android.gaming.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.android.R
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.android.gaming.data.AchievementWithOrganization
import it.lismove.app.android.gaming.repository.AchievementRepository
import it.lismove.app.android.gaming.ui.data.AchievementItemUI
import it.lismove.app.android.gaming.ui.data.AchievementUI
import it.lismove.app.android.gaming.ui.parser.asAchievementItemUI
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class AchievementViewModel(
    private val achievementRepository: AchievementRepository,
    private val userRepository: UserRepository,
    private val user: LisMoveUser
): ViewModel() {

    private val achievementFlow: MutableStateFlow<Lce<AchievementUI>> = MutableStateFlow(LceLoading())
    val achievementObservable = achievementFlow.asLiveData()

    private var initiatives: List<EnrollmentWithOrganization> = listOf()
    private var filter = arrayListOf(ListAlertData(null, "Tutte", "", imageRes = R.drawable.ic_notification_session),
        ListAlertData(null, "Community", "", imageRes = R.drawable.ic_notification_session), )
    private var achievements: List<Achievement> = listOf()

    fun getData(){
       viewModelScope.launch {
           try {
               achievementFlow.emit(LceLoading())
               initiatives = userRepository.getActiveInitiatives(user.uid)
               initFilter()
               filter.addAll(initiatives.map {
                   val imageRes = if(it.organization.notificationLogo.isNullOrEmpty()){
                       R.drawable.ic_notification_session
                   } else{
                       null
                   }
                   ListAlertData(it.organization.id.toString(), it.organization.title, "",
                       imageRes = imageRes, imageUrl = it.organization.notificationLogo)
                }
               )
               achievements = achievementRepository.getAchievements(user.uid)

               val achievementsUI = achievements.sortedBy { it.target }.map { it.asAchievementItemUI() }

               achievementFlow.emit(LceSuccess(
                   AchievementUI(
                       organizationTitle = null,
                       achievementTitle = "Tutte",
                       achievements = achievementsUI
                   )
               ))
           }catch (e: Exception){
               achievementFlow.emit(LceError(e))
           }
       }
   }

    fun initFilter(){
        filter = arrayListOf(ListAlertData(null, "Tutte", "", imageRes = R.drawable.ic_notification_session),
            ListAlertData(null, "Community", "", imageRes = R.drawable.ic_notification_session), )
    }
    fun getFilter(): List<ListAlertData>{
        return filter
    }

    fun applyFilter(filter: ListAlertData){
        viewModelScope.launch {
            achievementFlow.emit(LceLoading())
            val filteredItems = when(filter.leftText){
                "Tutte" -> achievements
                "Community" -> achievements.filter { it.organization == null }
                else -> {
                  val initiative =   initiatives.first { it.organization.title == filter.leftText }
                   achievements.filter { it.organization == initiative.organization.id }
                }
            }

            val filteredAchievementsUI = filteredItems.sortedBy { it.target }.map { it.asAchievementItemUI() }
            achievementFlow.emit(LceSuccess(
                    AchievementUI(
                        null,
                        filter.leftText,
                        filteredAchievementsUI
                    )

                )
            )
        }

    }

    fun getAchievement(item: AchievementItemUI): Achievement{
        return achievements.first { it.id.toString() == item.id }
    }
}
