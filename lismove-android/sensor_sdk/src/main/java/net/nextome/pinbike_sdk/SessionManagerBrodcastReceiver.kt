package net.nextome.lismove_sdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.style.TtsSpan
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking


class SessionManagerBrodcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.getStringExtra(NotificationProvider.NOTIFICATION_INTENT_ACTION)
        val sessionId =  intent?.getStringExtra(NotificationProvider.NOTIFICATION_INTENT_SESSION_ID)

        Log.d("actionReceived", intent?.extras.toString())
        if(!sessionId.isNullOrEmpty() && !action.isNullOrEmpty()){
            when(action){
                NotificationProvider.NOTIFICATION_INTENT_ACTION_STOP ->stopSession(sessionId)
            }
        }
    }

    private fun stopSession(id: String){
        runBlocking {
            LismoveSensorSdk().stop(id)
        }
    }
}