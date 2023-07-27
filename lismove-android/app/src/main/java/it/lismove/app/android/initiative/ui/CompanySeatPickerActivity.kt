package it.lismove.app.android.initiative.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import it.lismove.app.android.databinding.ActivityAddressPickerBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.adapter.AddressAdapter
import it.lismove.app.android.initiative.ui.data.AddressItemUI
import it.lismove.app.room.entity.SeatEntity
import org.koin.android.ext.android.inject
import timber.log.Timber

class CompanySeatPickerActivity : LisMoveBaseActivity(), LceView<List<AddressItemUI>> {

    companion object{
        const val REQUEST_CODE = 1002
        const val EXTRA_CITY = "ext_city"
        const val ORGANIZATION_ID = "ext_org_id"

        fun getIntent(ctx: Context, organizationID: Long): Intent{
            return Intent(ctx, CompanySeatPickerActivity::class.java).apply {
                putExtra(ORGANIZATION_ID, organizationID)
            }
        }
    }

    lateinit var binding: ActivityAddressPickerBinding
    lateinit var adapter: AddressAdapter

    val viewModel: CompanySeatPickerViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddressPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.seatsObservable.observe(this, LceDispatcher(this))
        viewModel.getSeats(intent)

        adapter = AddressAdapter(listOf()){
            onAddressClicked(it)
        }



        with(binding){
            backArrowImage.setOnClickListener { finish()}
            citiesRecyclerView.layoutManager = LinearLayoutManager(this@CompanySeatPickerActivity)
            citiesRecyclerView.adapter = adapter
            cityEditText.doOnTextChanged { text, start, before, count ->
                viewModel.filterSeats(text?.toString())
            }
        }

    }

    private fun onAddressClicked(address: AddressItemUI) {
        val seat = viewModel.getSeatEntity(address.id)
        returnResult(seat)
    }

    private fun returnResult(seat: SeatEntity){
        val intent = Intent().apply {
            putExtra(EXTRA_CITY, Gson().toJson(seat))
        }
        setResult(REQUEST_CODE, intent)
        finish()
    }

    override fun onLoading() {
        Timber.d("On Loading")
        binding.emptyListLabel.isVisible = false
        with(binding.loadingBar){
            isIndeterminate = true
            isVisible = true
        }
    }

    override fun onSuccess(data: List<AddressItemUI>) {
        binding.loadingBar.isVisible = false
        binding.emptyListLabel.isVisible = data.isEmpty()
        binding.citiesRecyclerView.isVisible = data.isEmpty().not()

        if(data.isEmpty().not()){
            showListLayout(data)
        }
    }

    fun showListLayout(data: List<AddressItemUI>){
        adapter.items = data
        adapter.notifyDataSetChanged()
    }


    override fun onError(throwable: Throwable) {
        binding.loadingBar.isVisible = false
        binding.emptyListLabel.isVisible = false
        showError(throwable.localizedMessage ?: "", binding.root)
    }
}