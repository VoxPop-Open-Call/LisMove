package it.lismove.app.android.authentication.repository

import android.provider.Settings.Global.getString
import android.widget.Toast
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import it.lismove.app.android.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.security.auth.callback.Callback
import com.facebook.login.LoginManager

import com.facebook.GraphResponse

import com.facebook.GraphRequest

import com.facebook.AccessToken





class AuthRepositoryImpl(): AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private  var auth = Firebase.auth
    private var authToken: String? = null

    override suspend fun isUserLogIn(): Boolean{
        return firebaseAuth.currentUser != null
    }

    override suspend fun getUserUid(): String{
        if(!isUserLogIn()) throw Exception("User is not logged in")
        return firebaseAuth.currentUser!!.uid
    }

    override fun getUserToken(): String {
        return authToken ?: ""
    }

    override suspend fun refreshUserToken(): String? {
        authToken = getCurrentAuthUser()?.getIdToken(true)?.await()?.token
        return authToken
    }

    override fun getCurrentAuthUser(): FirebaseUser?{
        return firebaseAuth.currentUser
    }

    override suspend fun getCurrentAuthUserReloaded(): FirebaseUser? {
        try{
            firebaseAuth.currentUser?.reload()?.await()
        }catch (error: Exception){
            Timber.d("getCurrentAuthUserReloaded: getting cached value")
        }
        return getCurrentAuthUser()
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        val authRes =  firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return authRes.user != null
    }

    override suspend fun signUp(email: String, password: String): String?{
        val res = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return res.user?.uid
    }


    override suspend fun firebaseAuthWithGoogle(idToken: String): String?{
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    override suspend fun firebaseAuthWithFacebook(token: AccessToken): String?{
        val credential =  FacebookAuthProvider.getCredential(token.token)
        return signInWithCredential(credential)
    }


    private suspend fun signInWithCredential(credential: AuthCredential): String?{
        val res = auth.signInWithCredential(credential).await()
        if(res.user != null && res.user?.email == null){
            auth.signOut()
            throw java.lang.Exception("Non è possibile usare l'account selezionato perchè non ha una mail impostata")
        }
        return res.user?.uid
    }

    override fun signOut() {
        auth.signOut()
        authToken = null
    }

    override fun sendResetPasswordToCurrentUser(){
        getCurrentAuthUser()?.email?.let {
            auth.sendPasswordResetEmail(it)
        }
    }

    override fun sendResetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    override suspend fun getFcmToken(): String{
        return FirebaseMessaging.getInstance().token.await()
    }

}