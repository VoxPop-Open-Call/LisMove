package it.lismove.app.android.authentication.useCases.impl

import com.google.firebase.auth.FirebaseUser
import it.lismove.app.room.entity.LisMoveUser
import it.lismove.app.android.authentication.repository.AuthRepository
import it.lismove.app.android.authentication.repository.PhoneRepository
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.authentication.useCases.AuthenticationUseCase
import it.lismove.app.android.authentication.useCases.data.*
import it.lismove.app.android.chat.ChatManager
import it.lismove.app.android.general.network.LismoveNetworkException
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.utils.TempPrefsRepository
import net.nextome.lismove_sdk.utils.BugsnagUtils
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.io.IOException
import java.lang.Exception

class AuthenticationUseCaseImpl(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val phoneRepository: PhoneRepository,
    private val chatManager: ChatManager,
    private val tempPrefsRepository: TempPrefsRepository,
): AuthenticationUseCase, KoinComponent {

    override suspend fun fetchUserAuthenticationState(): LoginState {
        val authUser = authRepository.getCurrentAuthUserReloaded()
        if(authUser == null){
            return LoginUnLogged()
        }else{
            BugsnagUtils.setUser(authUser.uid, authUser.email, authUser.displayName)
            if(authUser.isEmailVerified){
                val user = getLismoveUserFreshOrCached(authUser)
                if(user == null){
                    return CachedUserExpired(authUser.uid)
                }else{
                    return checkLisMoveUserProfile(user)
                }
            }else{
                val user = getLisMoveUserOrCreateOneIfNotExists(authUser)
                if(user.type == LisMoveUserResponse.LisMoveUserResponseType.NEW){
                    sendMailVerificationEmail(authUser)
                }
                return LoginEmailNotVerified(authUser.uid)
            }
        }
    }

    private fun sendMailVerificationEmail(authUser: FirebaseUser) {
        authUser.sendEmailVerification()
    }

    private suspend fun  getLismoveUserFreshOrCached(authUser: FirebaseUser): LisMoveUser?{
        return try{
            getLisMoveUserOrCreateOneIfNotExists(authUser).user
        } catch (e: LismoveNetworkException) {
                throw e
        }catch (e: IOException){
                val cachedProfile = userRepository.fetchUserProfile(authUser.uid)
                if(isCachedLoginValid(cachedProfile.lastLoggedIn)){
                    cachedProfile
                }else{
                    null
                }
            }

    }

    private suspend fun getLisMoveUserOrCreateOneIfNotExists(authUser: FirebaseUser): LisMoveUserResponse{
        try{
            return LisMoveUserResponse(userRepository.fetchUserProfileFromServer(authUser.uid),
                LisMoveUserResponse.LisMoveUserResponseType.SERVER
            )
        } catch (e: LismoveNetworkException){
            val emailAddress = authUser.email
            if((e.status == 401 || e.status == 404) && !emailAddress.isNullOrEmpty()){
                if(!userRepository.existsUser(emailAddress)){
                    val user =  createUserProfile(authUser)
                    return LisMoveUserResponse(user, LisMoveUserResponse.LisMoveUserResponseType.NEW)
                }
            }
            throw e
        }
    }


    private fun isCachedLoginValid(lastLogIn: Long?): Boolean {
        if(lastLogIn != null){
            Timber.d("isCachedLoginValid: $lastLogIn-${DateTimeUtils.daysPassed(lastLogIn)}")
            return (DateTimeUtils.daysPassed(lastLogIn)<2)
        }
        return false
    }

    private suspend fun checkLisMoveUserProfile(userProfile: LisMoveUser): LoginState{
        tempPrefsRepository.saveTempUser(userProfile)

        if(userProfile.termsAccepted){
            if(userProfile.isProfileComplete()){
                if(shouldBlockUser(userProfile)){
                    return LoginLoggedBlocked(userProfile.uid)
                }else{
                    updateUserProfile(userProfile)
                    chatManager.setUser(userProfile)

                    return LoginLoggedSuccess(userProfile.uid)
                }
            }else{
                return LoginLoggedProfileIncomplete(userProfile.uid)
            }
        }else{
            return LoginTermsNotAccepted(userProfile.uid)
        }
    }

    private suspend fun createUserProfile(authUser: FirebaseUser): LisMoveUser {
        Timber.d("creteUserProfile for user: ${authUser.toString()}")
        val email = authUser.email ?: throw Exception("Firebase user has email null")
        val uid = authUser.uid
        val user =  userRepository.createUserProfile(uid, email)
        tempPrefsRepository.saveTempUser(user)

        return user
    }

    private suspend fun updateUserProfile(lismoveUser: LisMoveUser){

        val currentPhone = phoneRepository.getInstallationIdentifier()
        lismoveUser.apply {
            signupCompleted = true
            resetPasswordRequired = false
            if(activePhone != currentPhone){
                activePhone = phoneRepository.getInstallationIdentifier()
                phoneActivationTime = DateTimeUtils.getCurrentTimestamp()
            }
            lastLoggedIn = DateTimeUtils.getCurrentTimestamp()
            activePhoneModel = phoneRepository.getDeviceName()
            activePhoneToken = authRepository.getFcmToken()
            activePhoneVersion = phoneRepository.getAppVersion()
        }
        try{
            userRepository.updateUserProfile(lismoveUser)
        }catch (e: IOException){
            Timber.d("No internet connection, update only locally")
        }
    }



    private suspend fun shouldBlockUser(lismoveUser: LisMoveUser): Boolean{
        return false/*
        val phoneIdentifier = phoneRepository.getInstallationIdentifier()
        val isDeviceInitialized = !lismoveUser.activePhone.isNullOrEmpty() && lismoveUser.phoneActivationTime != null
        if(phoneIdentifier == lismoveUser.activePhone || !isDeviceInitialized ){
            return false
        }else{
            val daysPassedSinceActivation = DateTimeUtils.daysPassed(lismoveUser.phoneActivationTime!!)
            return daysPassedSinceActivation < 7
        }*/
    }


    data class LisMoveUserResponse(
        val user: LisMoveUser,
        val type: LisMoveUserResponseType
    ){
        enum class LisMoveUserResponseType{
            CACHED, SERVER, NEW
        }
    }
}