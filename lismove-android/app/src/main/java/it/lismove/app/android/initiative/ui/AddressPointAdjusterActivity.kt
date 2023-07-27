package it.lismove.app.android.initiative.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAddressPointAdjusterBinding
import it.lismove.app.android.initiative.ui.data.WorkAddress
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class AddressPointAdjusterActivity: BasicPointAdjusterActivity(){
    lateinit var binding: ActivityAddressPointAdjusterBinding

    companion object {
        fun getIntent(ctx: Context, address: WorkAddress, isHomeAddress: Boolean): Intent{
            return Intent(ctx, AddressPointAdjusterActivity::class.java).apply {
                putExtra(INTENT_ADDRESS, Gson().toJson(address))
                putExtra(INTENT_IS_HOME_ADDRESS, isHomeAddress)
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == BasicPointAdjusterActivity.REQUEST_CODE) {
                Timber.d("POINTS UPDATED")
                val addressString =
                    result.data?.getStringExtra(BasicPointAdjusterActivity.INTENT_ADDRESS)

                val address = Gson().fromJson(addressString, WorkAddress::class.java)
                viewModel.workAddress = address
                save()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressPointAdjusterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity()
        setupAddressAutocomplete()
        viewModel.showAlertEvent.observe(this){
         //   showConfigAlert()
        }
        viewModel.viewModelScope.launch {
            delay(1000)
            showConfigAlert()
        }
        binding.addressHelpLayout.setOnClickListener { switchToManualPointAdjuster() }
    }

    private fun switchToManualPointAdjuster() {
        resultLauncher.launch(
            ManualAddressPointAdjusterActivity.getIntent(this, viewModel.workAddress, viewModel.isHomeAddress)
        )
    }


    fun setupAddressAutocomplete(){
     with(binding){
         val autocompleteFragment =
             supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                     as AutocompleteSupportFragment
         autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS))
         autocompleteFragment.setText(viewModel.workAddress.completeName)
         autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
             override fun onPlaceSelected(place: Place) {
                 Timber.d("Place: ${place.name}, ${place.id}")
                 viewModel.updateAddress(place)
             }

             override fun onError(status: Status) {
                 if(!status.isCanceled){
                     Timber.d("An error occurred: $status")
                     onError(Throwable(status.statusMessage))
                 }
             }
         })
     }
    }

    fun showConfigAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Per il corretto funzionamento è importante che la posizione indicata sia corretta, controlla il marker sulla mappa")
        builder.setPositiveButton(
            "ok"
        ) { dialog, id ->
           dialog.dismiss()
        }
        builder.show()
    }




}