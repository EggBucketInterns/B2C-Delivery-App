package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_frame_layout) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // Delay navigation until NavController is initialized
        binding.root.post {
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("FRAGMENT_TO_OPEN")?.let { fragmentName ->
            navigateToFragment(fragmentName)
        }
    }

    private fun navigateToFragment(fragmentName: String) {
        when (fragmentName) {
            "OrderNotification" -> {
                try {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        navController.navigate(R.id.dashboardFragment)
                    }
                    navController.navigate(R.id.action_dashboardFragment_to_newOrder)
                } catch (e: Exception) {
                    Log.e("NavigationError", "Failed to navigate: ${e.message}")
                }
            }
            else -> Log.d("IntentHandler", "Unknown fragment: $fragmentName")
        }
    }

}
