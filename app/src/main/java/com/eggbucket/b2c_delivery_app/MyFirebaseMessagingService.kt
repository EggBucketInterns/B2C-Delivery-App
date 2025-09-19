package com.eggbucket.b2c_delivery_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        val title: String?
        val body: String?

        // Prioritize the data payload, as it works consistently whether the app is
        // in the foreground or background.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: " + remoteMessage.data)
            title = remoteMessage.data["title"]
            body = remoteMessage.data["body"]
        } else {
            // Fallback to the notification payload if no data is present.
            Log.d("FCM", "Message Notification Body: ${remoteMessage.notification?.body}")
            title = remoteMessage.notification?.title
            body = remoteMessage.notification?.body
        }

        // If we successfully extracted a message body, process it.
        if (body != null) {
            val notificationEntity = parseNotificationMessage(body)
            saveNotificationToSharedPreferences(notificationEntity)
            val intent = createIntentFromNotification(notificationEntity)
            showNotification(title, body, intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Send this token to your backend server to associate it with the user
    }

    private fun showNotification(title: String?, message: String?, intent: Intent) {
        val channelId = "urgent_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri: Uri = "android.resource://${packageName}/raw/custom_sound".toUri()

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo1) // Ensure you have 'logo1.png' in your drawable folder
            .setContentTitle(title ?: "New Order Notification")
            .setContentText(message ?: "You have a new order.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Urgent Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for urgent order notifications"
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun saveNotificationToSharedPreferences(notificationEntity: NotificationEntity) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        // FIX: You must read from SharedPreferences *before* starting the edit transaction.
        val existingNotificationsJson = sharedPreferences.getString("notifications_list", null)

        // Now, begin the edit transaction with the data you've already read.
        sharedPreferences.edit {
            val gson = Gson()
            val type = object : TypeToken<MutableList<NotificationEntity>>() {}.type
            val notificationsList: MutableList<NotificationEntity> = if (existingNotificationsJson != null) {
                gson.fromJson(existingNotificationsJson, type)
            } else {
                mutableListOf()
            }

            notificationsList.add(0, notificationEntity) // Add to the beginning of the list

            val updatedNotificationsJson = gson.toJson(notificationsList)
            putString("notifications_list", updatedNotificationsJson)
        }

        Log.d("FCM", "Notification saved to SharedPreferences: $notificationEntity")
    }

    private fun createIntentFromNotification(notificationEntity: NotificationEntity): Intent {
        return Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_OPEN", "OrderNotification")
            putExtra("ORDER_ID", notificationEntity.orderId)
            putExtra("PICKUP", notificationEntity.pickup)
            putExtra("DELIVERY", notificationEntity.delivery)
            // Send numeric types in their native format for type safety.
            putExtra("ORDER_VALUE", notificationEntity.orderValue)
            putExtra("E6", notificationEntity.itemE6)
            putExtra("E12", notificationEntity.itemE12)
            putExtra("E30", notificationEntity.itemE30)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }

    private fun parseNotificationMessage(message: String): NotificationEntity {
        return try {
            val parts = message.split(";")
            val orderId = parts.getOrNull(0)?.substringAfter("Order ID:")?.trim() ?: "N/A"
            val status = parts.getOrNull(1)?.substringAfter("Status:")?.trim() ?: "N/A"
            val pickup = parts.getOrNull(2)?.substringAfter("Pickup:")?.trim() ?: "N/A"
            val delivery = parts.getOrNull(3)?.substringAfter("Delivery:")?.trim() ?: "N/A"
            val orderValue = parts.getOrNull(4)?.substringAfter("Order Value:")?.trim()?.toDoubleOrNull() ?: 0.0
            val itemsJsonString = parts.getOrNull(5)?.substringAfter("Items:")?.trim() ?: "{}"
            val itemsJson = JSONObject(itemsJsonString)

            NotificationEntity(
                orderId = orderId,
                status = status,
                pickup = pickup,
                delivery = delivery,
                orderValue = orderValue,
                itemE6 = itemsJson.optInt("E6", 0),
                itemE12 = itemsJson.optInt("E12", 0),
                itemE30 = itemsJson.optInt("E30", 0)
            )
        } catch (e: Exception) {
            Log.e("FCM_ParseError", "Error parsing notification message: $message", e)
            NotificationEntity("Error", "Error", "Error", "Error", 0.0, 0, 0, 0)
        }
    }
}
