package com.eggbucket.b2c_delivery_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.common.reflect.TypeToken
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson


import org.json.JSONObject

data class NotificationEntity(
    val orderId: String,
    val status: String,
    val pickup: String,
    val delivery: String,
    val orderValue: Double,
    val itemE6: Int,
    val itemE12: Int,
    val itemE30: Int,

)


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log the message
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            val messageBody = it.body ?: "No message body"
            Log.d("FCM", "Message Notification Body: $messageBody")

            // Parse the message body into a NotificationEntity
            val notificationEntity = parseNotificationMessage(messageBody)

            // Save the notification data to shared preference
            saveNotificationToSharedPreferences(notificationEntity)

            // Create an intent with the notification data
            val intent = createIntentFromNotification(notificationEntity)

            // Show the notification
            showNotification(it.title, it.body, intent)
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


    private fun saveNotificationToSharedPreferences(notificationEntity: NotificationEntity) {
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing notifications
        val gson = Gson()
        val existingNotificationsJson = sharedPreferences.getString("notifications_list", null)
        val type = object : TypeToken<MutableList<NotificationEntity>>() {}.type
        val notificationsList: MutableList<NotificationEntity> = if (existingNotificationsJson != null) {
            gson.fromJson(existingNotificationsJson, type)
        } else {
            mutableListOf()
        }

        // Add the new notification to the list
        notificationsList.add(notificationEntity)

        // Save the updated list back to SharedPreferences
        val updatedNotificationsJson = gson.toJson(notificationsList)
        editor.putString("notifications_list", updatedNotificationsJson)
        editor.apply()

        Log.d("FCM", "Notification saved to SharedPreferences: $notificationEntity")
    }


    private fun createIntentFromNotification(notificationEntity: NotificationEntity): Intent {
        return Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_OPEN", "OrderNotification")
            putExtra("ORDER_ID", notificationEntity.orderId)
            putExtra("PICKUP", notificationEntity.pickup)
            putExtra("DELIVERY", notificationEntity.delivery)
            putExtra("ORDER_VALUE", notificationEntity.orderValue.toString())
            putExtra("E6", notificationEntity.itemE6.toString())
            putExtra("E12", notificationEntity.itemE12.toString())
            putExtra("E30", notificationEntity.itemE30.toString())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
    private fun parseNotificationMessage(message: String): NotificationEntity {
        return try {
            val parts = message.split(";")
            val orderId = parts.getOrNull(0)?.substringAfter("Order ID:")?.trim() ?: "Unknown"
            val status = parts.getOrNull(1)?.substringAfter("Status:")?.trim() ?: "Unknown"
            val pickup = parts.getOrNull(2)?.substringAfter("Pickup:")?.trim() ?: "Unknown"
            val delivery = parts.getOrNull(3)?.substringAfter("Delivery:")?.trim() ?: "Unknown"
            val orderValue = parts.getOrNull(4)?.substringAfter("Order Value:")?.trim()?.toDoubleOrNull() ?: 0.0
            val itemsJson = parts.getOrNull(5)?.substringAfter("Items:")?.trim() ?: "{}"

            // Parse items JSON
            val items = JSONObject(itemsJson)
            val itemE6 = items.optInt("E6", 0)
            val itemE12 = items.optInt("E12", 0)
            val itemE30 = items.optInt("E30", 0)

            NotificationEntity(
                orderId = orderId,
                status = status,
                pickup = pickup,
                delivery = delivery,
                orderValue = orderValue,
                itemE6 = itemE6,
                itemE12 = itemE12,
                itemE30 = itemE30
            )
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationEntity(
                orderId = "Error",
                status = "Error",
                pickup = "Error",
                delivery = "Error",
                orderValue = 0.0,
                itemE6 = 0,
                itemE12 = 0,
                itemE30 = 0
            )
        }
    }


}






