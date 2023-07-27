package it.lismove.app.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.Map;

public class LisMoveFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Map extras = remoteMessage.getData();
        ZohoSalesIQ.Notification.handle(this.getApplicationContext(), extras, R.drawable.ic_logo);
    }

    @Override
    public void onNewToken(String token){
        ZohoSalesIQ.Notification.enablePush(token,true);
    }

}
