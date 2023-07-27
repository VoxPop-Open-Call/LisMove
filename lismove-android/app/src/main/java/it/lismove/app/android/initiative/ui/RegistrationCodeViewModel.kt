package it.lismove.app.android.initiative.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.general.lce.Lce
import it.lismove.app.android.general.lce.LceError
import it.lismove.app.android.general.lce.LceLoading
import it.lismove.app.android.general.lce.LceSuccess
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.initiative.ui.data.CodeEmptyError
import it.lismove.app.android.initiative.ui.data.CodeIncorrectError
import it.lismove.app.android.initiative.ui.data.EnrollmentFinished
import it.lismove.app.room.entity.EnrollmentEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class RegistrationCodeViewModel(
    val userRepository: UserRepository,
    var user: LisMoveUser
): ViewModel() {
    var isFromRegistration = false


    fun validateCode(code: String?) = flow<Lce<EnrollmentEntity>>{
        if(code.isNullOrEmpty()){
            emit(LceError(CodeEmptyError()))
        }else{
            emit(LceLoading())
            val enrollment = userRepository.verifyCode(user.uid, code)
            if(enrollment != null){
                if(DateTimeUtils.dateIsInFuture(enrollment.endDate)){
                    emit(LceSuccess(enrollment))
                }else{
                    emit(LceError(EnrollmentFinished()))
                }
            }else{
                emit(LceError(CodeIncorrectError()))
            }
        }
    }.catch { emit(LceError(it)) }.asLiveData()

    fun setupFromIntent(intent: Intent) {
        isFromRegistration = intent.getBooleanExtra(RegistrationCodeActivity.INTENT_IS_FROM_REGISTRATION, false)

    }
}