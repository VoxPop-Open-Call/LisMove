package it.lismove.app.android.initiative.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityAddressPointAdjusterBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception


abstract class BasicPointAdjusterActivity : LisMoveBaseActivity(), LceView<LatLng?>, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    companion object{
        val REQUEST_CODE = 1003
        val INTENT_ADDRESS = "intent_seat"
        val INTENT_IS_HOME_ADDRESS = "intent_is_home_address"


    }
    private lateinit var mMap: GoogleMap
     val placesClient: PlacesClient by lazy {
        Places.createClient(this)
    }
    val viewModel: AddressPointAdjusterViewModel by inject()
    var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun initActivity(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.setWorkAddress(intent)
        viewModel.latLngObservable.observe(this, LceDispatcher(this))

        val mapFragment = supportFragmentManager.findFragmentById(R.id.gMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.address_point_adjust_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
             R.id.address_point_adjust_save -> save()
        }
        return super.onOptionsItemSelected(item)
    }

    open fun onSaveRequest(){

    }


    fun save(){
        Timber.d("Save")
        onSaveRequest()
        if(viewModel.workAddress.isComplete(viewModel.ignoreNumber())){
            Timber.d("Address complete")
            val resIntent = Intent().apply {
                putExtra(INTENT_ADDRESS, Gson().toJson(viewModel.workAddress))
                putExtra(INTENT_IS_HOME_ADDRESS, viewModel.isHomeAddress)
            }
            setResult(REQUEST_CODE, resIntent)

            finish()
        }else{
            Timber.d("Address not complete")
            Snackbar.make(window.decorView, "Inserisci un indirizzo completo assicurandoti di aver inserito il numero civico", Snackbar.LENGTH_SHORT).show()
        }
        
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        Timber.d("onMapReady")
        mMap = map
        mMap.setOnMarkerDragListener(this)
        Places.initialize(applicationContext, getString(R.string.google_maps))
        viewModel.getLatLng(placesClient)


    }

    fun setPosition(position: LatLng){
        marker?.remove()
        val markerOptions = MarkerOptions().position(position).draggable(true)
        marker = mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18f))
    }

    override fun onMarkerDragStart(p0: Marker) {
        Timber.d("onMarkerDragStart, position is ${p0.position}")
    }

    override fun onMarkerDrag(p0: Marker) {

    }

    override fun onMarkerDragEnd(p0: Marker) {
        Timber.d("onMarkerDragEnd, position is ${p0.position}")
        viewModel.updatePosition(p0.position.latitude, p0.position.longitude)
    }

    override fun onLoading() {
        Timber.d("onLoading")
    }

    override fun onSuccess(data: LatLng?) {
        Timber.d("onSuccess")
        data?.let {
            setPosition(it)
        }
    }

    override fun onError(throwable: Throwable) {
        Timber.d(throwable.localizedMessage)
        BugsnagUtils.reportIssue(throwable)
        try {
            val autocompleteFragment =
                supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                        as? AutocompleteSupportFragment
            Toast.makeText(this@BasicPointAdjusterActivity, throwable.localizedMessage ?: "", Toast.LENGTH_LONG).show()
            autocompleteFragment?.setText(viewModel.workAddress.completeName)
        }catch (e: Exception){
            Timber.d(e.localizedMessage ?: "")
        }


    }

}