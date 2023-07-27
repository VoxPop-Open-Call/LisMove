package it.lismove.app.android.gaming.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.awards.AwardActivity
import it.lismove.app.android.gaming.ui.adapter.AchievementAdapter
import it.lismove.app.android.gaming.ui.data.AchievementItemUI
import it.lismove.app.android.gaming.ui.data.AchievementUI
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.data.ListAlertData
import org.koin.android.ext.android.inject
import timber.log.Timber

class AchievementFragment : ListWithSelectorFragment(), LceView<AchievementUI> {
    val viewModel: AchievementViewModel by inject()
    private val achievementAdapter = AchievementAdapter(listOf()){onAchievementClicked(it)}
    override var emptyString = "Nessuna coppa presente"
    override var infoText: String = "Le coppe sono dei premi ottenuti in base ai km o punti percorsi in un determinato periodo temporale."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Coppe"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.achievementObservable.observe(requireActivity(), LceDispatcher(this))
        viewModel.getData()

    }

    private fun onAchievementClicked(achievementItemUI: AchievementItemUI){
        val achievement = viewModel.getAchievement(achievementItemUI)
        startActivity(AwardActivity.getIntentFromAchievement(
            requireActivity(),
            achievementItemUI.id.toLong(),
            achievement.name,
            null
        ))
    }

    override fun onLoading() {
        showLoading()

    }

    override fun onSuccess(data: AchievementUI) {
        Timber.d("onSuccess ${data.achievements.size}")
        achievementAdapter.items = data.achievements
        achievementAdapter.notifyDataSetChanged()
        showSuccessGroup(data.achievements.isEmpty(), data.organizationTitle, data.achievementTitle)
    }

    override fun onError(throwable: Throwable) {
       showError(throwable)
    }

    override fun setRecyclerViewAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = achievementAdapter
    }

    override fun changeFilter(filter: ListAlertData){
        viewModel.applyFilter(filter)
    }

    override fun getFilterList(): List<ListAlertData> {
        return viewModel.getFilter()
    }


}