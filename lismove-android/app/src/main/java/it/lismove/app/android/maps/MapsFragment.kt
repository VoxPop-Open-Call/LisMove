package it.lismove.app.android.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewModelScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.github.zawadz88.materialpopupmenu.MaterialPopupMenuBuilder
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.maps.android.clustering.ClusterManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentMapsBinding
import it.lismove.app.android.general.LisMoveFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.MarkerOptions
import it.lismove.app.android.maps.data.FountainClusterItem
import it.lismove.app.android.maps.data.InitiativePolygon


class MapsFragment : LisMoveFragment(R.layout.fragment_maps), OnMapReadyCallback {
    val viewModel: MapViewModel by inject()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    var previousPolyline: Polyline? = null
    var initiativePolygons = arrayListOf<Polygon>()
    private lateinit var clusterManager: ClusterManager<FountainClusterItem>

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.filter { it.value}.size == permissions.size){
            onPermissionGranted()
        }
    }

    private val loadingDialog: SweetAlertDialog by lazy {
        SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            this.progressHelper.barColor =  AppCompatResources.getColorStateList(requireContext(), R.color.red_main).defaultColor
            this.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            it.title = "Mappa"

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        binding = FragmentMapsBinding.bind(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.gMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onPermissionGranted(){
        updatePosition()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
       loadMapData()

    }

    fun showPolygon(data: List<InitiativePolygon>){
        removeCurrentPolygons()
        data.forEach {
            addPolygon(it.polygon, it.id)
        }
    }


    @SuppressLint("MissingPermission")
    fun updatePosition(){
        Timber.d("Update positionCalled")
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                  it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch( arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION ))
                return
            }
            mMap.isMyLocationEnabled = true
            loadMapOnUserPosition()
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun loadMapData(){
        initClusterManager()
        viewModel.viewModelScope.launch {
            val initiativePolygons = viewModel.getInitiativePolygon()
            showPolygon(initiativePolygons)
            mMap.setOnMarkerClickListener(clusterManager)
            mMap.setOnCameraIdleListener(clusterManager)
        }
        binding.settingsButton.setOnClickListener { onSettingsButtonClicked(it) }
        viewModel.gpsConnectivity.observe(this){
            if(it){
                updatePosition()
            }else{
                val title = "Servizio di localizzazione disabilitato"
                val message =
                    "Se visualizzare correttamente la posizione attiva il servizio di localizzazione GPS"
                showAlertDialog(title, message)
            }
        }
        binding.addFountainButton.setOnClickListener { openAddFountainDialog() }
    }

    private fun openAddFountainDialog() {
        val dialog = AddFountainFragment()
        dialog.onFinished = {
            update ->
            if(update){
                refreshDrinkingFountain()
            }
        }
        dialog.show(
            childFragmentManager, "PurchaseConfirmationDialogTag")
    }

    fun refreshDrinkingFountain(){
        viewModel.viewModelScope.launch {
            viewModel.fetchDrinkingFountains()
            if(viewModel.showFountain){
                clusterManager.clearItems()
                updateDrinkingFountain()
            }
        }
    }

    private fun initClusterManager(){
        context?.let {
            clusterManager = ClusterManager(it, mMap)
            clusterManager.renderer = DrinkingFountainClusterManager(it, mMap, clusterManager)

            clusterManager.setOnClusterItemClickListener {
                showDrinkingFountainActions(it.position)
                return@setOnClusterItemClickListener true
            }
            clusterManager.setAnimation(false)
        }

    }

    private fun loadDrinkingFountain(){

        viewModel.viewModelScope.launch {
            val fountains = viewModel.fetchDrinkingFountains()

            fountains.forEach {
                clusterManager.addItem(it)
            }
            clusterManager.cluster()
        }
    }


    private fun loadMapOnUserPosition(){

        loadDrinkingFountain()
        positionMapOnCurrentLocation()

        activity?.let {
            viewModel.setSessionFromIntent(it.intent)
            viewModel.pathLiveData.observe(it ){
                onPathChanged(it)
            }
        }


    }


    private fun positionMapOnCurrentLocation(){
        viewModel.viewModelScope.launch {
            context?.let { context ->
                viewModel.getLastLocation(context)?.let {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
                }
            }

        }
    }

    private fun onPathChanged(path: List<LatLng>){
        if(path.isNotEmpty()){
            previousPolyline?.remove()
            val polylineOption = PolylineOptions()
                .clickable(false)
                .addAll(path)
            previousPolyline = mMap.addPolyline(polylineOption)
            stylePolyline(previousPolyline!!)
            mMap.addMarker(
                MarkerOptions()
                    .position(path.first())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_route_start_marker))
            )
        }
    }


    //TODO: Per il momento sono tutte rosse, cambiare il colore
    private fun stylePolyline(polyline: Polyline) {
        polyline.width = 10f
        polyline.color = ResourcesCompat.getColor(resources, R.color.red_main, null)
        polyline.jointType = JointType.ROUND
    }

    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private fun stylePolygon(polygon: Polygon) {
        polygon.strokeColor = Color.RED
        polygon.strokeWidth = 2f
        polygon.fillColor = Color.parseColor("#30FF0000")
    }

    private fun addPolygon(polygon: List<List<LatLng>>, tag: String){
        polygon.forEach {
            if(it.isNotEmpty()){
                val polygon = mMap.addPolygon(
                    PolygonOptions()
                        .clickable(true)
                        .addAll(it)


                )
                stylePolygon(polygon)
                polygon.tag = tag
                initiativePolygons.add(polygon)
            }

        }

    }

    private fun removePolygon(tag: String){
        initiativePolygons.filter { it.tag == tag }.forEach {
            it.remove()
            initiativePolygons.remove(it)
        }
    }



    private fun onSettingsButtonClicked(view: View) {
        val popupMenu = popupMenu {
            dropdownGravity = Gravity.END
            section {
                for (initiative in viewModel.initiativePolygons){
                    customItem {
                        layoutResId = R.layout.view_custom_item_checkable
                        viewBoundCallback = { view ->
                            val switch: SwitchMaterial = view.findViewById(R.id.itemSwitch)
                            switch.text = initiative.initiative
                            switch.isChecked = initiative.isVisible
                            switch.setOnCheckedChangeListener { buttonView, isChecked ->
                                initiative.isVisible = isChecked
                                updateInitiativePolygon(initiative)
                            }
                        }
                    }
                }

                customItem {
                    layoutResId = R.layout.view_custom_item_checkable
                    viewBoundCallback = { view ->
                        val switch: SwitchMaterial = view.findViewById(R.id.itemSwitch)
                        switch.text = "Fontanelle"
                        switch.isChecked = viewModel.showFountain
                        switch.setOnCheckedChangeListener { buttonView, isChecked ->
                            viewModel.showFountain = isChecked
                            updateDrinkingFountain()
                        }
                    }
                }

            }
        }

        popupMenu.show(requireContext(), view)
    }



    private fun updateInitiativePolygon(initiative: InitiativePolygon){
        if(initiative.isVisible){
            addPolygon(initiative.polygon, initiative.id)
        }else{
            removePolygon(initiative.id)
        }
    }

    private fun removeCurrentPolygons(){
        initiativePolygons.forEach { it.remove() }
    }

    private fun updateDrinkingFountain(){
        Timber.d("updateDrinkingFountain ${viewModel.showFountain}")
        if(viewModel.showFountain){
                clusterManager.addItems(viewModel.drinkingFountainsUI)
                clusterManager.cluster()
        }else{
            clusterManager.clearItems()
            clusterManager.cluster()
        }
    }


    private fun navigateToCoordinates(coordinates: LatLng){
        val mapsUri =
            Uri.parse("google.navigation:q=${coordinates.latitude}, ${coordinates.longitude}&mode=b")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun showDrinkingFountainActions(position: LatLng){
        context?.let {
            val colors = arrayOf("Navigare verso la fontanella", "Segnala la fontanella come inesistente", "Chiudi")
            val builder: AlertDialog.Builder = AlertDialog.Builder(it)

            builder.setItems(colors) { dialog, which ->
                when(which){
                    0 -> navigateToCoordinates(position)
                    1 -> deleteFountain(position)
                    else -> Timber.d("")
                }
                dialog.dismiss()
            }
            builder.show()

        }

    }

    private fun deleteFountain(position: LatLng) {
        loadingDialog.show()
        viewModel.viewModelScope.launch {
            try {
                val removedItem = viewModel.deleteFountain(position)
                clusterManager.removeItem(removedItem)
                showSuccessAlert()

            }catch (e: Exception){
                showErrorAlert(e)
            }
        }

    }

    private fun showErrorAlert(e: Exception) {
        loadingDialog.apply {
            setTitleText("Si è verificato un errore")
            setContentText(e.localizedMessage)
            setConfirmText("Ok")
            setConfirmClickListener {
                it.dismissWithAnimation()


            }
            changeAlertType(SweetAlertDialog.ERROR_TYPE)
        }
    }

    private fun showSuccessAlert() {
        loadingDialog.apply {
            setTitleText("Fontanella rimossa!")
            setContentText("Grazie per la segnalazione, la fontanella è stata rimossa")
            setConfirmText("Ok")
            setConfirmClickListener {
                it.dismissWithAnimation()
                clusterManager.cluster()
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        }
    }


}

