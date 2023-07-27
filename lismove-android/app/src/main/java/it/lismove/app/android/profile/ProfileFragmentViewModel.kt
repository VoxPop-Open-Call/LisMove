package it.lismove.app.android.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.android.general.lce.LceError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ProfileFragmentViewModel(
    var user: LisMoveUser,
    var userRepository: UserRepository
): ViewModel() {

        private val stateFlow = MutableStateFlow<Lce<LisMoveUser>>(LceSuccess(user))
        val stateLiveData = stateFlow.asLiveData()

        fun updatePhoto(url: Uri,){
                viewModelScope.launch {
                        stateFlow.emit(LceLoading())
                        try {
                                user = userRepository.updateUserImage( url, user)
                                stateFlow.emit(LceSuccess(user))
                        }catch(error: Exception){
                              stateFlow.emit(LceError(error))
                        }

                }
        }
}

