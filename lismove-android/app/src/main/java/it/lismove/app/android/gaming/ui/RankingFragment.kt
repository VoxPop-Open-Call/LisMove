package it.lismove.app.android.gaming.ui

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import it.lismove.app.android.R
import it.lismove.app.android.awards.AwardActivity
import it.lismove.app.android.databinding.FragmentRankingBinding

import it.lismove.app.android.gaming.ui.adapter.RankingAdapter
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.initiative.ui.view.InitiativeRegulationAlertUtils
import it.lismove.app.android.initiative.ui.view.ListAlertDialog
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class RankingFragment : LisMoveFragment(R.layout.fragment_ranking), LceView<RankingDetailUI> {


    private lateinit var binding: FragmentRankingBinding
    val viewModel: RankingViewModel by inject()
    val rankingAdapter = RankingAdapter(listOf())
    var selectedRanking: RankingDetailUI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Classifiche"

    }

    fun setupUI(data: RankingDetailUI){
        Timber.d("Ranking size ${data.ranking.size}")
        selectedRanking = data
        rankingAdapter.items = data.ranking
        showAward(data.hasPrize)
        rankingAdapter.notifyDataSetChanged()
        with(binding.rankingTitleBar){
            rankingPicker.setOnClickListener { showRankingPicker() }
            rankingTitle.text = data.organizationTitle
            rankingTitle.isVisible = !data.organizationTitle.isNullOrEmpty()

            daysLayout.visibility = if(data.dayTillEnd.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            daysLeft.text = data.dayTillEnd
            rankingSubtitle.text = data.rankingTitle
            rankingSubtitle.visibility = if(data.rankingTitle.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE

            infoButton.isVisible = true
            infoButton.setOnClickListener {
                openInfo()
            }
        }

        binding.emptyLayout.isVisible = data.ranking.isEmpty()
        binding.userPlacement.root.isVisible = data.userPlacement != null
        binding.view2.isVisible = data.userPlacement != null

        data.userPlacement?.let {
            with(binding.userPlacement){
                position.text = data.userPlacement.position
                positionLabel.text = data.userPlacement.positionLabel
                userName.text = data.userPlacement.username
                userName.typeface = Typeface.DEFAULT_BOLD
                points.text = data.userPlacement.points
                pointsLabel.text = data.userPlacement.pointsLabel
                root.setOnClickListener {
                    goToUserPosition(data.userPlacement.position.toInt())
                }
                if(data.userPlacement.picProfile.isNullOrEmpty()){
                    val avatarPic = AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_fab)
                    picProfile.setImageDrawable(avatarPic)

                }else{
                    picProfile.load(data.userPlacement.picProfile){
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }


    }

    fun openInfo(){
        context?.let { context ->
            viewModel.selectedRanking?.organization?.let {
                InitiativeRegulationAlertUtils.getAlertDialog(
                    context,
                    viewModel.selectedRanking?.organizaitonRegulation,
                    viewModel.selectedRanking?.organizationName ?: ""
                ).show()
            }
        }

    }

    fun goToUserPosition(position: Int){
        binding.rankingRecyclerView.scrollToPosition(position)
    }

    fun showAward(hasPrize: Boolean){
        binding.awardLayout.isVisible = hasPrize
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRankingBinding.bind(view)
        showAward(false)
        activity?.let { activity ->
            with(binding.rankingRecyclerView){
                layoutManager = LinearLayoutManager(activity)
                adapter = rankingAdapter
                val itemDecoration =  DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
                addItemDecoration(itemDecoration)
            }
            binding.awardLayout.setOnClickListener { goToRankingAwards() }
            viewModel.rankingObservable.observe(activity, LceDispatcher(this))
            //binding.rankingTitleBar.infoButton.setImageDrawable(AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_award_icon))
            viewModel.loadData()
        }

    }


    override fun onLoading() {
        with(binding){
            loadingBar.isIndeterminate = true
            loadingBar.isVisible = true
            successGroup.isVisible = false
            emptyLayout.isVisible = false
        }
    }

    override fun onSuccess(data: RankingDetailUI) {
        with(binding){
            loadingBar.isVisible = false
            successGroup.isVisible = true
            emptyLayout.isVisible = false

            setupUI(data)

        }
    }

    fun showRankingPicker(){
        activity?.let {
            try{
                val dialog = ListAlertDialog.build(it, "Seleziona classifica") {item, dialog ->
                    Timber.d("DISMISS")
                    changeRanking(item)
                }
                dialog.setData(viewModel.getRankings())
                dialog.show()
            }catch (e: Exception){
                onError(e)
            }
        }


    }

    fun changeRanking(item: ListAlertData){
        viewModel.changeRanking(item)
    }

    override fun onError(throwable: Throwable) {
        with(binding){
            loadingBar.isVisible = false
            successGroup.isVisible = false
            /*emptyLayout.text = "Classifica non disponibile"
            emptyLayout.isVisible = true*/
        }
        Timber.d("${throwable.localizedMessage}")
        showError(throwable.message ?: "Si è verificato un errore")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }
    private fun goToRankingAwards(){
        activity?.let {
            selectedRanking?.let {
                if(it.hasPrize){
                    startActivity(AwardActivity.getIntentFromRanking(requireActivity(), it.id!!, it.organizationTitle, it.rankingTitle))
                }
            }
        }

    }
}