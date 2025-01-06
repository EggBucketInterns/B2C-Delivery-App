package com.eggbucket.b2c_delivery_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            super.onMessageReceived(remoteMessage)

            // Log the message
            Log.d("FCM", "Message received from: ${remoteMessage.from}")

            // Check if the message contains data payload
            remoteMessage.data.isNotEmpty().let {
                Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            }
            val orderId = remoteMessage.data["ORDER_ID"]
            val pickup = remoteMessage.data["PICKUP"]
            val delivery = remoteMessage.data["DELIVERY"]
            val orderValue=remoteMessage.data["ORDER_VALUE"]
            val e6=remoteMessage.data["E6"]
            val e12=remoteMessage.data["E12"]
            val e30=remoteMessage.data["E30"]


            // Pass the extracted data to MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("FRAGMENT_TO_OPEN", "OrderNotification")
                putExtra("ORDER_ID", orderId)
                putExtra("PICKUP", pickup)
                putExtra("DELIVERY", delivery)
                putExtra("ORDER_VALUE",orderValue)
                putExtra("E6",e6)
                putExtra("E12",e12)
                putExtra("E30",e30)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Check if the message contains a notification payload
            remoteMessage.notification?.let {
                Log.d("FCM", "Message Notification Body: ${it.body}")
                showNotification(it.title, it.body,intent)
            }
        }

        override fun onNewToken(token: String) {
            super.onNewToken(token)
            Log.d("FCM", "New token: $token")
            // Send token to your server if needed
        }

    private fun showNotification(title: String?, message: String?,intent: Intent) {
        val channelId = "urgent_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set the custom notification sound
        val soundUri: Uri = Uri.parse("android.resource://${packageName}/raw/custom_sound")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo1) // Replace with a valid icon
            .setContentTitle(title ?: "Notification")
            .setContentText(message ?: "Message content")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Configure the notification channel
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default notification channel for app"
                setSound(soundUri, attributes) // Set custom sound with high volume
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    }


