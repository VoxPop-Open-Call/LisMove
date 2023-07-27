package it.lismove.app.android.authentication.repository

import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface AuthRepository {
    fun getCurrentAuthUser(): FirebaseUser?
    suspend fun getCurrentAuthUserReloaded(): FirebaseUser?
    suspend fun isUserLogIn(): Boolean
    suspend fun getUserUid(): String
    fun getUserToken(): String
    suspend fun refreshUserToken(): String?
    suspend fun signIn(email: String, password: String):Boolean
    suspend fun signUp(email: String, password: String): String?
    suspend fun firebaseAuthWithGoogle(idToken: String): String?
    suspend fun firebaseAuthWithFacebook(token: AccessToken): String?
    fun sendResetPasswordToCurrentUser()
    fun sendResetPassword(email:String)
    fun signOut()
    suspend fun getFcmToken(): String
}