package it.lismove.app.android.authentication.useCases

import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.general.lce.Lce
import kotlinx.coroutines.flow.Flow

interface EditProfileUseCase {

    fun updateProfile(user: LisMoveUser): Flow<Lce<LisMoveUser>>


}