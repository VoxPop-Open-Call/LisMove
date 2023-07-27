package it.lismove.app.android.initiative.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.CityPickerActivity
import it.lismove.app.android.databinding.ActivityManualAddressPointAdjusterBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.initiative.ui.BasicPointAdjusterActivity
import it.lismove.app.android.initiative.ui.data.WorkAddress
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber


class ManualAddressPointAdjusterActivity: BasicPointAdjusterActivity(){
    lateinit var binding: ActivityManualAddressPointAdjusterBinding
    companion object {
        fun getIntent(ctx: Context, address: WorkAddress, isHomeAddress: Boolean): Intent{
            return Intent(ctx, ManualAddressPointAdjusterActivity::class.java).apply {
                putExtra(INTENT_ADDRESS, Gson().toJson(address))
                putExtra(INTENT_IS_HOME_ADDRESS, isHomeAddress)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualAddressPointAdjusterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity()
        setInitialWokAddressIfPresent()
        with(binding){
            refreshButton.setOnClickListener {
                updateViewModelAddress()
            }
            addressHelpLayout2.setOnClickListener {
                startActivity(AddressPointAdjusterActivity.getIntent(this@ManualAddressPointAdjusterActivity, viewModel.workAddress, viewModel.isHomeAddress))
                finish()
            }
            viewModel.viewModelScope.launch {
                delay(1000)
                showConfigAlert()
            }
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

    override fun onSaveRequest() {
        super.onSaveRequest()
        val address = binding.addressTextField.text?.toString()
        val number = binding.addressNumberTextField.text?.toString()
        viewModel.updateAddressAndNumber(address, number)

    }

    private fun updateViewModelAddress(){

        val address = binding.addressTextField.text?.toString()
        val number = binding.addressNumberTextField.text?.toString()
        viewModel.updateAddressAndGetLocation(address, number, placesClient)
    }
    private fun setInitialWokAddressIfPresent() {
        with(binding){
           updateAddressUI()
            addressCityTextField.setOnClickListener {
                updateViewModelAddress()
                openCityPickerActivity()
            }
        }
    }

    fun updateAddressUI(){
        with(binding){
            addressTextField.setText(viewModel.workAddress.address)
            addressNumberTextField.setText(viewModel.workAddress.number)
            addressCityTextField.setText(viewModel.workAddress.cityExtended?.getFullName())
        }

    }

    private fun openCityPickerActivity(){
        val intent = Intent(this, CityPickerActivity::class.java)
        startActivityForResult(intent, CityPickerActivity.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult $requestCode")
        if(requestCode == CityPickerActivity.REQUEST_CODE){
            viewModel.setCityFormIntent(data, placesClient)
            updateAddressUI()

        }
    }


}