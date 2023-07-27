package it.lismove.app.android.awards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.R
import it.lismove.app.android.awards.AwardActivity
import it.lismove.app.android.awards.AwardDetailActivity
import it.lismove.app.android.awards.AwardsUI
import it.lismove.app.android.awards.adapter.AwardAdapter
import it.lismove.app.android.awards.data.AwardItemUI
import it.lismove.app.android.databinding.FragmentAwardWrapperBinding
import it.lismove.app.android.gaming.ui.ListWithSelectorFragment
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.data.ListAlertData
import org.koin.android.ext.android.inject


class AwardWrapperFragment : ListWithSelectorFragment(), LceView<AwardsUI> {
    lateinit var binding: FragmentAwardWrapperBinding
    override var showSelector = false
    override var emptyString = "Nessun premio presente"
    override var showInfo = false
    override var showDivider = true
    val awardAdapter =  AwardAdapter(listOf()){item ->
            openAwardDetail(item)
    }

    val viewModel: AwardWrapperViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Premi"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.awardObservable.observe(requireActivity(), LceDispatcher(this))
        activity?.intent?.let {
            viewModel.getData(it)
        }

    }

    override fun setRecyclerViewAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = awardAdapter
    }

    override fun changeFilter(filter: ListAlertData) {

    }

    override fun getFilterList(): List<ListAlertData> {
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

    private fun openAwardDetail(item: AwardItemUI) {
        context?.let {
            val award = viewModel.getAwardFromIndex(item.id.toInt())
            startActivity(AwardDetailActivity.getUserAwardIntent(requireActivity(), award))
        }

    }

}