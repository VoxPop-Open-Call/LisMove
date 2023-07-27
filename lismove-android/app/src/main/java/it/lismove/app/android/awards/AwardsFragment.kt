package it.lismove.app.android.awards

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.awards.adapter.AwardAdapter
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.gaming.ui.ListWithSelectorFragment
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.data.ListAlertData
import org.koin.android.ext.android.inject

class AwardsFragment : ListWithSelectorFragment(), LceView<AwardsUI> {

    val viewModel: AwardViewModel by inject()
    val awardAdapter =  AwardAdapter(listOf()){goToAwardDetail(it)}
    override var showSelector = false
    override var showInfo = false
    override var emptyString = "Nessun premio presente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Premi"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initFromIntent(requireActivity().intent)
        viewModel.awardObservable.observe(requireActivity(), LceDispatcher(this))
        viewModel.getData()
    }

    override fun setRecyclerViewAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = awardAdapter
    }

    override fun changeFilter(filter: ListAlertData) {
        //viewModel.applyFilter(filter)
    }

    override fun getFilterList(): List<ListAlertData> {
        //return viewModel.getFilter()
        return listOf()
    }

    override fun onLoading() {
        showLoading()
    }

    override fun onSuccess(data: AwardsUI) {
        awardAdapter.items = data.items
        awardAdapter.notifyDataSetChanged()
        showSuccessGroup(data.items.isEmpty(), data.title, data.subtitle)
    }

    override fun onError(throwable: Throwable) {
        showError(throwable)
    }

    fun goToAwardDetail(data: AwardItemUI){
        if(viewModel.type == AwardActivity.INTENT_TYPE_RANKING){
            val award = viewModel.getRankingAward(data.id)
            startActivity(AwardDetailActivity.getRankingAwardIntent(
                requireActivity(),
                award
            ))
        }else{
            val award = viewModel.getAchievementAward(data.id)
            startActivity(AwardDetailActivity.getAchievementAwardIntent(
                requireActivity(),
                award
            ))
        }
    }

}