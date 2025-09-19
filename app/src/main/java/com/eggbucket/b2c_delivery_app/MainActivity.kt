package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.eggbucket.b2c_delivery_app.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val status = sharedPreferences.getString("status", "default")

        // FIX: Point the Intent to your new LoginActivity class
        if (status != "logged_in") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        fetchApiData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent()
    }

    private fun fetchApiData() {
        // Your existing API fetching logic is fine.
        // This function will now be called only for logged-in users.
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
            setupNavigation()
        }, 1000)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        binding.progressBar.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_frame_layout) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController
            NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
            handleNotificationIntent()
        } else {
            Log.e("MainActivity", "FATAL: NavHostFragment not found.")
            showError("Could not initialize navigation.")
        }
    }

    private fun handleNotificationIntent() {
        if (intent?.getStringExtra("FRAGMENT_TO_OPEN") == "OrderNotification") {
            val bundle = Bundle().apply {
                putString("ORDER_ID", intent.getStringExtra("ORDER_ID") ?: "N/A")
                putString("PICKUP", intent.getStringExtra("PICKUP") ?: "N/A")
                putString("DELIVERY", intent.getStringExtra("DELIVERY") ?: "N/A")
                putDouble("ORDER_VALUE", intent.getDoubleExtra("ORDER_VALUE", 0.0))
                putInt("E6", intent.getIntExtra("E6", 0))
                putInt("E12", intent.getIntExtra("E12", 0))
                putInt("E30", intent.getIntExtra("E30", 0))
            }

            if (::navController.isInitialized) {
                navController.navigate(R.id.action_global_to_newOrder, bundle)
            } else {
                Log.e("MainActivity", "NavController not initialized when trying to handle intent.")
            }
            intent.removeExtra("FRAGMENT_TO_OPEN")
        }
    }
}