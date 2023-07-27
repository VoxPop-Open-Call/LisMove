package it.lismove.app.android.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentAddFountainBinding
import it.lismove.app.android.maps.data.FountainClusterItem
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class AddFountainFragment : DialogFragment(), OnMapReadyCallback,  GoogleMap.OnMarkerDragListener {
    private lateinit var mMap: GoogleMap
    var positionMarker: Marker? = null
    val viewModel: AddFountainViewModel by inject()
    lateinit var binding: FragmentAddFountainBinding
    private lateinit var clusterManager: ClusterManager<FountainClusterItem>
    var onFinished: (update: Boolean) -> Unit = {}
    private val loadingDialog: SweetAlertDialog by lazy {
        SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            this.progressHelper.barColor =  AppCompatResources.getColorStateList(requireContext(), R.color.red_main).defaultColor
            this.setCancelable(false)
        }
    }

    companion object{
        const val TAG = "Fragment"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFountainBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.gMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.addFountainDismissButton.setOnClickListener {
            onFinished(false)
            dismiss()
        }

    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }


    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.filter { it.value}.size == permissions.size){
            onPermissionGranted()
        }
    }

    private fun onPermissionGranted(){
        updatePosition()
    }



    private fun initClusterManager(){
        context?.let {
            clusterManager = ClusterManager(it, mMap)
            clusterManager.renderer = DrinkingFountainClusterManager(it, mMap, clusterManager)
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
            onMapReadyWithUserPosition()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onMapReadyWithUserPosition(){
        //mMap.isMyLocationEnabled = true

        binding.addFountainConfirmButton.setOnClickListener { saveDrinkingFountain() }
        viewModel.viewModelScope.launch {
            context?.let { context ->
                viewModel.getLastLocation(context)?.let {
                   setPosition(it)
                }
            }

        }
        initClusterManager()
        loadDrinkingFountain()

    }


    private fun saveDrinkingFountain() {
        viewModel.viewModelScope.launch {
            loadingDialog.show()
            try {
                viewModel.saveDrinkingFountain()
                onDrinkingFountainSaved()
            }catch (e: Exception){
               onDrinkingFountainSaveError(e)
            }
        }
    }

    private fun onDrinkingFountainSaved(){
        loadingDialog.apply{
            setTitleText("Fontanella aggiunta!")
            setContentText("Grazie per la segnalazione, la tua fontanella è stata aggiunta alla mappa")
            setConfirmText("Ok")
            setConfirmClickListener {
                it.dismissWithAnimation()
                onFinished(true)
                this@AddFountainFragment.dismiss()
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        }
    }

    private fun onDrinkingFountainSaveError(error: Exception){
        loadingDialog.apply{
            setTitleText("Si è verificato un errore")
            setContentText(error.localizedMessage)
            setConfirmText("Ok")
            setConfirmClickListener {
                it.dismissWithAnimation()
                onFinished(false)
                this@AddFountainFragment.dismiss()
            }
            changeAlertType(SweetAlertDialog.ERROR_TYPE)
        }
    }


    private fun setPosition(position: LatLng){
        positionMarker?.remove()
        val markerOptions = MarkerOptions()
            .position(position)
            .draggable(true)

        positionMarker = mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18f))
        viewModel.changeMarkerPosition(position)
    }



    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        Timber.d("onMapReady")
        mMap = map
        updatePosition()
    }


    override fun onMarkerDragStart(p0: Marker) {
        Timber.d("onMarkerDragStart, position is ${p0.position}")
    }

    override fun onMarkerDrag(p0: Marker) {

    }

    override fun onMarkerDragEnd(p0: Marker) {
        Timber.d("onMarkerDragEnd, position is ${p0.position}")
        viewModel.changeMarkerPosition(p0.position)
    }
}