package it.lismove.app.android.gaming.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentActiveAwardsBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.adapter.SimpleStringAdapter
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.view.InitiativeRegulationAlertUtils
import org.koin.android.ext.android.inject


class ActiveAwardsFragment : LisMoveFragment(R.layout.fragment_active_awards),
    LceView<List<SimpleItem>> {

    private lateinit var binding: FragmentActiveAwardsBinding
    val viewModel: ActiveAwardsViewModel by inject()

    private val initiativeAdapter = SimpleStringAdapter(listOf()){
        showAlertDialog(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Premi e incentivi"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentActiveAwardsBinding.bind(view)
        binding.activeAwardsLoading.isIndeterminate = true
        activity?.let { activity ->
            with(binding.recyclerView){
                layoutManager = LinearLayoutManager(activity)
                adapter = initiativeAdapter
            }

            viewModel.stateObservable.observe(activity, LceDispatcher(this))
            viewModel.getState()
        }

    }


    override fun onLoading() {
        binding.activeAwardsLoading.isVisible = true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSuccess(data: List<SimpleItem>) {
        binding.activeAwardsLoading.isVisible = false
        initiativeAdapter.items = data
        initiativeAdapter.notifyDataSetChanged()
    }

    override fun onError(throwable: Throwable) {
        showError(throwable.localizedMessage)
    }


    private fun showAlertDialog(initiativeData: SimpleItem) {
        activity?.let {
            val initiative = viewModel.getInitiativeFromItem(initiativeData)
            InitiativeRegulationAlertUtils.getAlertDialog(
                ctx = it,
                regulation = initiative.organization.getSanitizedRegulation(),
                organizationName = initiative.organization.title
            ).show()

        }

    }


}