package it.lismove.app.android.deviceConfiguration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.databinding.FragmentCarPickerPageBinding
import it.lismove.app.android.general.adapter.SimpleStringAdapter
import it.lismove.app.android.general.adapter.data.SimpleItem
import timber.log.Timber


class FragmentCarPickerPage(
    var page: CarPickerPage,
) : Fragment() {
    private var filteredElements = page.list
    private var binding: FragmentCarPickerPageBinding? = null
    private var itemsAdapter: SimpleStringAdapter = SimpleStringAdapter(listOf(), page.onItemSelected)
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentCarPickerPageBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.configRecyclerView?.adapter = itemsAdapter
        binding?.configRecyclerView?.layoutManager = LinearLayoutManager(requireActivity())
        updateUI()
    }



    fun updateUI(){
        Timber.d("list is ${page.list?.size}")
        itemsAdapter = SimpleStringAdapter(page.list ?: listOf(), page.onItemSelected)
        binding?.configRecyclerView?.adapter = itemsAdapter

        binding?.let {
            with(it) {
                configTitle.text = page.title
                configDescription.text = page.description
                configEditText.hint = page.textFieldHint
                configEditText.setText(page.textFieldText)
                configEditTextLayout.isVisible  = page.textFieldHint.isNullOrEmpty().not()
                configEditText.setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus){
                        page.expanded = true
                        page.loadRecyclerView()
                        updateExpandDependandLayout()
                    }
                }
                configEditText.addTextChangedListener { editable ->
                    filteredElements = page.list?.filter { it.data.contains(editable.toString()) }
                    filteredElements?.let {
                        itemsAdapter.items = it
                        itemsAdapter.notifyDataSetChanged()
                    }
                }

                loadingIndicator.isIndeterminate = true
                loadingIndicator.isVisible = page.isLoading
                configRecyclerView.isVisible = page.isLoading.not()
                setImage(topImage,page.image)
                updateExpandDependandLayout()
                setCarConfigurationResume(page.carConfiguration)

            }
        }

    }

    private fun setCarConfigurationResume(car: CarConfiguration?){
        binding?.let {
            with(it) {
                    carConfigResume.root.isVisible = car != null
                    car?.let {
                        configRecyclerView.isVisible = false
                        mainImageLayout.isVisible = false
                        with(carConfigResume){
                            removeButton.isVisible = false
                            brandText.text = it.brand
                            modelText.text = it.model
                            generationText.text = it.generation
                            modificationText.text = it.modification
                        }
                    }

            }
        }

    }
    private fun setImage(view: ImageView, res: Int?){
        if(res != null){
            view.visibility = View.VISIBLE
            view.setImageDrawable(ResourcesCompat.getDrawable(resources, res, null))
        }else{
            view.visibility = View.GONE
        }

    }

    fun updateExpandDependandLayout(){
        binding?.let {
            with(it){
                mainImageLayout.isVisible = page.expanded.not()
                configRecyclerView.isVisible = page.expanded
            }
        }

    }

}

data class CarPickerPage(
    val title: String,
    val description: String,
    var showTextField: Boolean = false,
    var textFieldHint: String?,
    var textFieldText: String?,
    val image: Int,
    val isLoading: Boolean = false,
    var list: List<SimpleItem>? = null,
    var loadRecyclerView: () -> Unit = {},
    var onItemSelected: (item: SimpleItem) -> Unit = {},
    var expanded: Boolean = false,
    var carConfiguration: CarConfiguration? = null
)

data class CarConfiguration(
    var brand: String,
    var model: String,
    var generation: String,
    var modification: String
)