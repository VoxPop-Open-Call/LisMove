package it.lismove.app.android.car.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.car.data.CarModificationExpanded
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.room.entity.LisMoveUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CarConfigurationViewModel(
    private val userRepository: UserRepository,
    private val user: LisMoveUser,
): ViewModel() {
    var car: CarModificationExpanded? = null

    fun getCarData(): LiveData<Lce<CarModificationExpanded?>> = flow {
        emit(LceLoading())
        car = userRepository.getUserCar(user.uid)
        emit(LceSuccess(car))
    }.catch { emit(LceError(it)) }.asLiveData()

    fun removeCar(): LiveData<Lce<String>> = flow{
        emit(LceLoading())
        delay(2000)
        userRepository.deleteUserCar(user.uid)
        emit(LceSuccess("Car rimossa con successo"))
    }.catch { emit(LceError(it)) }.asLiveData()

}
