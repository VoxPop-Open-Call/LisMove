package it.lismove.app.android.car.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import cn.pedant.SweetAlert.SweetAlertDialog
import it.lismove.app.android.R
import it.lismove.app.android.authentication.ui.Loading
import it.lismove.app.android.car.data.CarModificationExpanded
import it.lismove.app.android.databinding.ActivityCarConfigurationBinding
import it.lismove.app.android.general.activity.LisMoveBaseActivity
import it.lismove.app.android.general.lce.*
import org.koin.android.ext.android.inject

class CarConfigurationActivity : LisMoveBaseActivity(), LceView<CarModificationExpanded?> {
    val viewModel: CarConfigurationViewModel by inject()
    lateinit var binding: ActivityCarConfigurationBinding
    var carData: LiveData<Lce<CarModificationExpanded?>>? = null
    var dialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.configureButton.setOnClickListener {
            startActivity(Intent(this, CarWizardActivity::class.java))
        }
        loadCarData()
    }

    override fun onResume() {
        super.onResume()
        loadCarData()
    }

    private fun loadCarData(){
        carData?.removeObservers(this)
        carData = viewModel.getCarData()
        carData?.observe(this, LceDispatcher(this))
    }

    override fun onLoading() {
        with(binding){
            loadingBar.isIndeterminate = true
            loadingBar.visibility = View.VISIBLE
            readyGroup.visibility = View.GONE
        }
    }

    override fun onSuccess(data: CarModificationExpanded?) {
        binding.loadingBar.visibility = View.GONE
        if(data != null){
            showCarUI(data)
        }else{
            showEmptyCarUI()
        }
    }

    override fun onError(throwable: Throwable) {
        showError(throwable.localizedMessage ?: "Si è verificato un errore", binding.root )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showCarUI(car: CarModificationExpanded){
        with(binding){
            configureButton.text = getString(R.string.riconfigure_car_button_label)
            configureButton.visibility = View.VISIBLE
            carLayout.visibility = View.VISIBLE
            with(carConfigurationItem){
                modelText.text = car.generation.model.name
                brandText.text = car.generation.model.brand.name
                generationText.text = car.generation.modelYear.toString()
                modificationText.text = car.getModificationDescription()
                removeButton.setOnClickListener {
                    viewModel.removeCar().observe(this@CarConfigurationActivity, {
                        removeCar(it)
                    })
                }
            }

        }
    }

    fun removeCar(result: Lce<String>){
        when(result){
            is LceLoading -> { showAlertLoading()}
            is LceSuccess -> { showAlertSuccess(result.data)}
            is LceError -> { showAlertError(result.error)}
        }
    }

    fun showEmptyCarUI(){
        with(binding){
            configureButton.text = getString(R.string.configure_car_button_label)
            carLayout.visibility = View.GONE
            configureButton.visibility = View.VISIBLE
            devicedNotConfiguredGroup.visibility = View.VISIBLE
        }
    }

    fun showAlertLoading(){
        dialog = SweetAlertDialog(this).apply {
            setTitleText("Caricamento")
            changeAlertType(SweetAlertDialog.PROGRESS_TYPE)
            show()
        }
    }

    fun showAlertSuccess(desc: String) {
        dialog?.apply{
            setTitleText("Congratulazioni")
            setContentText(desc)
            setConfirmText("Contintua")
            setConfirmClickListener {
                it.dismissWithAnimation()
                loadCarData()
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        }
    }

    fun showAlertError(throwable: Throwable) {
        dialog?.apply{
            setTitleText(throwable.message)
            changeAlertType(SweetAlertDialog.ERROR_TYPE)
            show()
        }
    }
}