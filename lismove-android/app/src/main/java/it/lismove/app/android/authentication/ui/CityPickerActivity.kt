package it.lismove.app.android.authentication.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.adapter.CityAdapterCallback
import it.lismove.app.android.authentication.ui.adapter.CityRecyclerViewAdapter
import it.lismove.app.android.authentication.ui.data.CityItemUI
import it.lismove.app.android.databinding.ActivityCityPickerBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class CityPickerActivity : AppCompatActivity(), CityAdapterCallback {
    companion object{
        const val REQUEST_CODE = 1001
        const val EXTRA_CITY = "ext_cityName"
        const val EXTRA_CITY_TAG = "EXTRA_CITY_TAG"
    }

    lateinit var binding: ActivityCityPickerBinding
    lateinit var cityRecyclerViewAdapter: CityRecyclerViewAdapter
    val viewModel: CityPickerViewModel by inject()
    var cityTag = -1L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCityPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val citiesObservable = viewModel.fetchCities()
        cityRecyclerViewAdapter = CityRecyclerViewAdapter(listOf(), this)
        cityTag = intent.getLongExtra(EXTRA_CITY_TAG, -1L)
        citiesObservable.observe(this){
            Timber.d("items ${it.size}")
             binding.citiesRecyclerView.adapter= CityRecyclerViewAdapter(it, this)

        }

        with(binding){
            backArrowImage.setOnClickListener { finish()}
            citiesRecyclerView.layoutManager = LinearLayoutManager(this@CityPickerActivity)
            citiesRecyclerView.adapter = cityRecyclerViewAdapter
            cityEditText.doOnTextChanged { text, start, before, count ->
                viewModel.filterCities(text?.toString() ?: "")
            }
        }

    }

    override fun onCityClicked(city: CityItemUI) {
        returnResult(city)
    }

    private fun returnResult(city:CityItemUI){
        val intent = Intent().apply {
            putExtra(EXTRA_CITY, Gson().toJson(viewModel.getCityFromId(city.id)))
            putExtra(EXTRA_CITY_TAG, cityTag)
        }
        setResult(REQUEST_CODE, intent)
        finish()
    }


}