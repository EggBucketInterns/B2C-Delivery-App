package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.eggbucket.b2c_delivery_app.databinding.ActivityMainBinding
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val status = sharedPreferences.getString("status", "default")

        if (status != "logedin") {
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

        // Start API call
        fetchApiData()
        setupNavigation()
    }

    private fun fetchApiData() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/098765421") // Replace with your API URL
            .build()

        var attempt = 0
        val maxAttempts = 3

        fun makeRequest() {
            attempt++
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    if (attempt < maxAttempts) {
                        makeRequest() // Retry the request
                    } else {
                        runOnUiThread {
                            showError("Failed to load data after $maxAttempts attempts. Please try again.")
                        }
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            // Hide progress bar and show main content
                            binding.progressBar.visibility = View.GONE
                            binding.mainContent.visibility = View.VISIBLE

                            // Setup navigation
                            setupNavigation()
                        }
                    } else {
                        if (attempt < maxAttempts) {
                            makeRequest() // Retry the request
                        } else {
                            runOnUiThread {
                                showError("Failed to load data. Please try again.")
                            }
                        }
                    }
                }
            })
        }

        // Start the first request
        makeRequest()
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupNavigation() {


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_frame_layout) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)


        handleIntentExtras()
    }

    private fun handleIntentExtras() {
        val fragmentToOpen = intent.getStringExtra("FRAGMENT_TO_OPEN")
        val orderId = intent.getStringExtra("ORDER_ID")
        val pickup = intent.getStringExtra("PICKUP")
        val delivery = intent.getStringExtra("DELIVERY")
        val orderValue = intent.getStringExtra("ORDER_VALUE")
        val e6 = intent.getStringExtra("E6")
        val e12 = intent.getStringExtra("E12")
        val e30 = intent.getStringExtra("E30")

        if (fragmentToOpen == "OrderNotification") {
            val bundle = Bundle().apply {
                putString("ORDER_ID", orderId)
                putString("PICKUP", pickup)
                putString("DELIVERY", delivery)
                putString("ORDER_VALUE", orderValue)
                putString("E6", e6)
                putString("E12", e12)
                putString("E30", e30)
            }
            navController.navigate(R.id.action_dashboardFragment_to_newOrder, bundle)
        }
    }
}

