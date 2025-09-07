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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences // Declare SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Authentication Check ---
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val status = sharedPreferences.getString("status", "default")

        if (status != "logged_in") {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Inflate layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show progress bar and hide main content initially
        binding.progressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        // Start API call using dynamic user ID
        fetchApiData()
    }

    private fun fetchApiData() {
        // --- START RECOMMENDED FIX ---

        // 1. Retrieve the saved phone number (user ID) from SharedPreferences.
        val phoneNo = sharedPreferences.getString("phone_no", null)

        // 2. Validate the ID. If it's null, something went wrong during login.
        if (phoneNo == null) {
            showError("User session error. Please log in again.")
            // Redirect back to Login to force re-authentication
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 3. Build the URL dynamically with the correct user ID.
        val apiUrl = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/$phoneNo"

        // --- END RECOMMENDED FIX ---

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(apiUrl) // Use the dynamic URL, not a hardcoded one.
            .build()

        var attempt = 0
        val maxAttempts = 3

        fun makeRequest() {
            attempt++
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (attempt < maxAttempts) {
                        makeRequest() // Retry the request
                    } else {
                        runOnUiThread {
                            showError("Failed to load data after $maxAttempts attempts. Please try again.")
                            // Show content even if initial ping fails, let fragments handle their own data loading.
                            binding.progressBar.visibility = View.GONE
                            binding.mainContent.visibility = View.VISIBLE
                            setupNavigation()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        // Hide progress bar and show main content regardless of success,
                        // as long as we get a response from the server.
                        binding.progressBar.visibility = View.GONE
                        binding.mainContent.visibility = View.VISIBLE

                        if (response.isSuccessful) {
                            Log.d("MainActivity", "Initial data ping successful.")
                        } else {
                            Log.w("MainActivity", "Initial data ping failed with code: ${response.code}")
                        }
                        // Setup navigation after showing content.
                        setupNavigation()
                    }
                }
            })
        }

        // Start the first request
        makeRequest()
    }

    private fun showError(message: String) {
        // Avoid showing progress bar in error state if it's already hidden.
        if (binding.progressBar.visibility == View.VISIBLE) {
            binding.progressBar.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE // Show content to display error context if needed
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_frame_layout) as NavHostFragment
        navController = navHostFragment.navController // Initialize class-level navController here
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        handleIntentExtras()
    }

    private fun handleIntentExtras() {
        val fragmentToOpen = intent.getStringExtra("FRAGMENT_TO_OPEN")
        if (fragmentToOpen == "OrderNotification") {
            val orderId = intent.getStringExtra("ORDER_ID")
            val pickup = intent.getStringExtra("PICKUP")
            val delivery = intent.getStringExtra("DELIVERY")
            val orderValue = intent.getStringExtra("ORDER_VALUE")
            val e6 = intent.getStringExtra("E6")
            val e12 = intent.getStringExtra("E12")
            val e30 = intent.getStringExtra("E30")

            val bundle = Bundle().apply {
                putString("ORDER_ID", orderId)
                putString("PICKUP", pickup)
                putString("DELIVERY", delivery)
                putString("ORDER_VALUE", orderValue)
                putString("E6", e6)
                putString("E12", e12)
                putString("E30", e30)
            }
            // Ensure navController is initialized before navigating.
            if (::navController.isInitialized) {
                navController.navigate(R.id.action_dashboardFragment_to_newOrder, bundle)
            } else {
                Log.e("MainActivity", "NavController not initialized when trying to handle intent extras.")
            }
        }
    }
}