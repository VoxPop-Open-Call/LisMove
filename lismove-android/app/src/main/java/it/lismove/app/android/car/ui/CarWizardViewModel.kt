package it.lismove.app.android.car.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.car.data.*
import it.lismove.app.android.car.repository.CarRepository
import it.lismove.app.android.general.adapter.data.SimpleItem
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CarWizardViewModel(
    private val userRepository: UserRepository,
    private val carRepository: CarRepository,
    private val user: LisMoveUser
): ViewModel() {

    var selectedBrand: CarBrand? = null
    var selectedModel: CarModel? = null
    var selectedGeneration: CarGeneration? = null
    var selectedModification: CarModification? = null

    var brands: List<CarBrand>? = null
    var models: List<CarModel>? = null
    var modifications: List<CarModification>? = null
    var generations: List<CarGeneration>? = null


    fun getBrand(): LiveData<Lce<List<SimpleItem>>> = flow{
        emit(LceLoading())
        brands = carRepository.getBrands()
        emit(LceSuccess(brands!!.map { SimpleItem(it.id, it.name) }))
    }.asLiveData()

    fun getModel():  LiveData<Lce<List<SimpleItem>>> = flow {
        emit(LceLoading())
        models = carRepository.getModels(selectedBrand!!.id)
        emit(LceSuccess(models!!.map { SimpleItem(it.id.toString(), it.name) }))
    }.asLiveData()

    fun getCarGenerations(): LiveData<Lce<List<SimpleItem>>> = flow{
        emit(LceLoading())
        generations = carRepository.getGenerations(selectedBrand!!.id, selectedModel!!.id.toString())
        emit(LceSuccess(generations!!.map { SimpleItem(it.id.toString(), it.name) }))
    }.asLiveData()

    fun getCarModifications(): LiveData<Lce<List<SimpleItem>>> = flow{
        emit(LceLoading())
        modifications = carRepository.getModifications(selectedBrand!!.id, selectedModel!!.id.toString(), selectedGeneration!!.id.toString())
        emit(LceSuccess(modifications!!.map { SimpleItem(it.id.toString(), it.getModificationDescription()) }))
    }.asLiveData()

    fun selectBrand(brand: SimpleItem){
        selectedBrand = brands!!.first { it.id == brand.id }
    }
    fun selectModel(model: SimpleItem){
        selectedModel = models!!.first { it.id == model.id.toLong() }
    }
    fun selectGeneration(generation: SimpleItem){
        selectedGeneration = generations!!.first { it.id == generation.id.toLong() }
    }
    fun selectModification(modification: SimpleItem){
        selectedModification = modifications!!.first { it.id == modification.id.toLong() }
    }

     fun saveUserCar(): LiveData<Lce<String>> = flow{
        emit(LceLoading())
         if(selectedModification == null){
             emit(LceError(Error("Modification is null")))
             return@flow
         }
        userRepository.setUserCar(user.uid, selectedModification!!)
        emit(LceSuccess("Auto configurata con successo"))
    }.catch {emit(LceError(it))  }.asLiveData()
}
