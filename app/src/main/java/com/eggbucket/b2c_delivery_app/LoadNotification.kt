package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class LoadNotification : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("FCM", "opened loadnotification")
        setContentView(R.layout.nactivity_load_notification)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("FRAGMENT_TO_OPEN", "OrderNotification")
        startActivity(intent)
        finish()
    }
}



