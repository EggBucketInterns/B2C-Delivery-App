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
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.eggbucket.b2c_delivery_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_frame_layout) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // Check if the intent contains a specific fragment to navigate to
        val fragmentToOpen = intent.getStringExtra("FRAGMENT_TO_OPEN")
        val orderId = intent.getStringExtra("ORDER_ID")
        val pickup = intent.getStringExtra("PICKUP")
        val delivery = intent.getStringExtra("DELIVERY")
        val orderValue=intent.getStringExtra("ORDER_VALUE")
        val e6=intent.getStringExtra("E6")
        val e12=intent.getStringExtra("E12")
        val e30=intent.getStringExtra("E30")

        // Navigate to the appropriate fragment if the intent specifies one
        if (fragmentToOpen == "OrderNotification") {
            val bundle = Bundle().apply {
                putString("ORDER_ID", orderId)
                putString("PICKUP", pickup)
                putString("DELIVERY", delivery)
                putString("ORDER_VALUE",orderValue)
                putString("E6",e6)
                putString("E12",e12)
                putString("E30",e30)

            }
            navController.navigate(R.id.action_dashboardFragment_to_newOrder, bundle)
        }
    }


}
