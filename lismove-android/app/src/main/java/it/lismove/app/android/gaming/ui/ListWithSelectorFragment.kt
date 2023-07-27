package it.lismove.app.android.gaming.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentListWithSelectorBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.initiative.ui.data.ListAlertData
import it.lismove.app.android.initiative.ui.view.ListAlertDialog
import timber.log.Timber


abstract class ListWithSelectorFragment : LisMoveFragment(R.layout.fragment_list_with_selector){
    private lateinit var binding: FragmentListWithSelectorBinding
    open var showSelector = true
    open var emptyString = ""
    open var showInfo = true
    open var showDivider = true
    open var infoText = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Coppe"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListWithSelectorBinding.bind(view)
        binding.prizeTitleBar.infoButton.visibility = if (showInfo) View.VISIBLE else View.INVISIBLE
        binding.prizeTitleBar.infoButton.setOnClickListener { openDialog() }
        with(binding.prizeRecyclerView){
            layoutManager = LinearLayoutManager(requireActivity())
            setRecyclerViewAdapter(this)
            if(showDivider){
                val itemDecoration =  DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
                addItemDecoration(itemDecoration)
            }

        }
    }

    abstract fun setRecyclerViewAdapter(recyclerView: RecyclerView)
    abstract fun changeFilter(filter: ListAlertData)
    abstract fun getFilterList(): List<ListAlertData>

    fun showLoading() {
        Timber.d("onLoading")
        binding.loadingBar2.isIndeterminate = true
        binding.loadingBar2.isVisible = true
        binding.successGroup.isVisible = false
        binding.emptyGroup.isVisible = false

    }

    fun  showSuccessGroup(isEmpty: Boolean, title: String?, subtitle: String?) {
        binding.loadingBar2.isVisible = false
        binding.emptyGroup.isVisible = isEmpty
        binding.successGroup.isVisible = isEmpty.not()
        binding.emptyText.text = emptyString
        setupTitleBar(title, subtitle)
    }

    private fun setupTitleBar(title: String?, subtitle: String?){
        with(binding.prizeTitleBar){
            rankingPicker.setOnClickListener { showFilterPicker(getFilterList()) }
            rankingPicker.isVisible = showSelector
            rankingTitle.text = title
            rankingTitle.isVisible = !title.isNullOrEmpty()

            daysLayout.isVisible = false
            rankingSubtitle.text = subtitle
            rankingSubtitle.isVisible = !subtitle.isNullOrEmpty()
        }
    }

     fun showError(throwable: Throwable) {
        Timber.d("onError ${throwable.localizedMessage}")
        binding.loadingBar2.isVisible = false

    }

    fun showFilterPicker(filterList: List<ListAlertData>){
        val dialog = ListAlertDialog.build(requireContext(), "Seleziona iniziativa") { item, dialog ->
            changeFilter(item)
        }
        dialog.setData(filterList)
        dialog.show()
    }

    fun openDialog(){
        if (infoText.isNullOrEmpty().not()){
            showAlertDialog("Regolamento", infoText)
        }
    }

}