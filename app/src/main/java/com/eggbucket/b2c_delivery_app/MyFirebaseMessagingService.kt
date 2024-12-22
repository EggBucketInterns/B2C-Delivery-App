package com.eggbucket.b2c_delivery_app;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "Device Token: " + token);
        // Send the token to your server or save it locally
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Handle incoming FCM messages if needed
        Log.d("FCM", "Message Received: " + remoteMessage.getData());
    }
}
