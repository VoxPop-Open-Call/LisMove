package it.lismove.app.android.car.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import cn.pedant.SweetAlert.SweetAlertDialog
import it.lismove.app.android.R
import it.lismove.app.android.databinding.ActivityDeviceConfigurationBinding
import it.lismove.app.android.deviceConfiguration.CarConfiguration
import it.lismove.app.android.deviceConfiguration.CarPickerPage
import it.lismove.app.android.deviceConfiguration.FragmentCarPickerPage
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.initiative.ui.data.CodeEmptyError
import it.lismove.app.android.initiative.ui.data.CodeIncorrectError
import it.lismove.app.android.initiative.ui.data.EnrollmentFinished
import it.lismove.app.room.entity.EnrollmentEntity
import org.koin.android.ext.android.inject
import timber.log.Timber

class CarWizardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceConfigurationBinding

    val viewModel: CarWizardViewModel by inject()
    var dialog: SweetAlertDialog? = null
    var pages: List<FragmentCarPickerPage> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pages = listOf(CarPickerPage("Aiuta a risparmiare C02",
            "Seleziona tutti i dati del tuo veicolo e stimeremo la c02 risparmiata",
            false,
            null,
            null,
            R.drawable.co2),
                            CarPickerPage("Iniziamo dal Brand",
                                "Inizia a digitare il nome del brand",
                                true,
                                "Cerca brand",
                                null, R.drawable.car_1,
                                loadRecyclerView = {getBrand()},
                                onItemSelected = {onBrandSelected(it)}),
                            CarPickerPage("Ora cerchiamo il modello",
                                "Ci aiuterà ad individuare tutti i dati necessari",
                                true,
                                "Cerca modello",
                                null,
                                R.drawable.car_2,
                                loadRecyclerView = {getModel()}
                                , onItemSelected = {onModelSelected(it)}),
                            CarPickerPage("Ora cerchiamo la generazione",
                                "Ci aiuterà ad individuare tutti i dati necessari",
                                true,
                                "Cerca anno",
                                null,
                                R.drawable.car_3, loadRecyclerView = {getGenerations()}, onItemSelected = {onGenerationSelected(it)}),
                            CarPickerPage("Infine selezioniamo la configurazione",
                                "Effettueremo una stima sulla base dei dati raccolti",
                                true,
                                "Cerca configurazione",
                                null,
                                R.drawable.car_2,
                                loadRecyclerView = {getModifications()},
                                onItemSelected = {onModificationSelected(it)}),
                            CarPickerPage("Ecco Fatto",
                                "Questi sono i dati inseriti, registrare l'automobile?\n",
                                false,
                                null,
                                null,
                                R.drawable.car_2,
                                loadRecyclerView = { },
                                onItemSelected = {},
                                carConfiguration = CarConfiguration("",
                                    "",
                                    "",
                                    "")
                            )

            ).map { FragmentCarPickerPage(it) }
        val adapter = CarPickerAdapter(this, pages)
        with(binding){
            configurationPager.adapter = adapter
            configDotIndicator.setViewPager2(configurationPager)
            configClose.setOnClickListener { finish() }
            configButton.setOnClickListener { goToNextPage() }
            configurationPager.isUserInputEnabled = false
            configDotIndicator.dotsClickable = false
            configurationPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    pages[position].updateUI()
                    Timber.d("OnPageSelected $position")
                    updateCommonViewBasedOnPage()
                }
            })

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
                finish()
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

    private fun getGenerations() {
        val fragment = pages[3]

        viewModel.getCarGenerations().observe(this){
            when(it){
                is LceLoading -> {
                    fragment.page = fragment.page.copy(isLoading = true)
                    fragment.updateUI()
                }
                is LceSuccess -> {
                    fragment.page = fragment.page.copy(isLoading = false, list = it.data)
                    fragment.updateUI()
                }
                is LceError ->{
                    fragment.page = fragment.page.copy(isLoading = false)
                    fragment.updateUI()
                    Timber.e("Errore nella ricezione del brand")
                }

            }
        }
    }


    fun getBrand(){
        val fragment = pages[1]

        viewModel.getBrand().observe(this){
            when(it){
                is LceLoading -> {
                    fragment.page = fragment.page.copy(isLoading = true)
                    fragment.updateUI()
                }
                is LceSuccess -> {
                    fragment.page = fragment.page.copy(isLoading = false, list = it.data)
                    fragment.updateUI()
                }
                is LceError ->{
                    fragment.page = fragment.page.copy(isLoading = false)
                    fragment.updateUI()
                    Timber.e("Errore nella ricezione del brand")
                }

            }
        }
    }

    fun getModel(){
        val fragment = pages[2]
        viewModel.getModel().observe(this){
            when(it){
                is LceLoading -> {
                    fragment.page = fragment.page.copy(isLoading = true)
                    fragment.updateUI()
                }
                is LceSuccess -> {
                    fragment.page = fragment.page.copy(isLoading = false, list = it.data)
                    fragment.updateUI()
                }
                is LceError ->{
                    fragment.page = fragment.page.copy(isLoading = false)
                    fragment.updateUI()
                    Timber.e("Errore nella ricezione del brand")
                }

            }
        }
    }

    private fun getModifications() {
        val fragment = pages[4]

        viewModel.getCarModifications().observe(this){
            when(it){
                is LceLoading -> {
                    fragment.page = fragment.page.copy(isLoading = true)
                    fragment.updateUI()
                }
                is LceSuccess -> {
                    fragment.page = fragment.page.copy(isLoading = false, list = it.data)
                    fragment.updateUI()
                }
                is LceError ->{
                    fragment.page = fragment.page.copy(isLoading = false)
                    fragment.updateUI()
                    Timber.e("Errore nella ricezione del brand")
                }

            }
        }
    }

    fun onBrandSelected(item: SimpleItem){
        viewModel.selectBrand(item)
        pages[1].page.expanded = false
        pages[1].page.textFieldText = item.data
        updateCarResume()
        goToNextPage()
    }

    fun onModelSelected(item: SimpleItem){
        viewModel.selectModel(item)
        pages[2].page.expanded = false
        pages[2].page.textFieldText = item.data
        updateCarResume()
        goToNextPage()
    }

    fun onGenerationSelected(item: SimpleItem){
        viewModel.selectGeneration(item)
        pages[3].page.expanded = false
        pages[3].page.textFieldText = item.data
        updateCarResume()
        goToNextPage()
    }

    fun onModificationSelected(item: SimpleItem){
        viewModel.selectModification(item)
        pages[4].page.expanded = false
        pages[4].page.textFieldText = item.data
        updateCarResume()
        goToNextPage()

    }

    fun goToNextPage(){
        with(binding){
            if(configurationPager.currentItem == pages.size -1){
                saveCar()
            }else{
                configurationPager.currentItem = configurationPager.currentItem + 1
                updateCommonViewBasedOnPage()
            }

        }
    }

    fun saveCar(){
        viewModel.saveUserCar().observe(this, {
            when(it){
                is LceLoading -> showAlertLoading()
                is LceSuccess -> showAlertSuccess(it.data)
                is LceError -> showAlertError(it.error)
            }
        })
    }

    fun updateCommonViewBasedOnPage(){
        val enableNext = shouldEnableNext()
        binding.configButton.isVisible = enableNext
    }

    private fun shouldEnableNext(): Boolean {
        val page = pages[binding.configurationPager.currentItem]
        return (page.page.showTextField && page.page.textFieldText.isNullOrEmpty()
            .not()) || page.page.showTextField.not()
    }

    fun updateCarResume(){
        pages[5].page.apply {
            carConfiguration?.brand = viewModel.selectedBrand?.name ?: ""
            carConfiguration?.model = viewModel.selectedModel?.name ?: ""
            carConfiguration?.generation = viewModel.selectedGeneration?.name ?: ""
            carConfiguration?.modification = viewModel.selectedModification?.getModificationDescription() ?: ""
        }
    }

}