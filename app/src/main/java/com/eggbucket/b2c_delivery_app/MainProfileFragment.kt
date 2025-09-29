package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MainProfileFragment : Fragment() {

    private lateinit var personNameTextView: TextView
    private lateinit var phoneNoTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_profile, container, false)

        // --- Initialization ---
        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        personNameTextView = view.findViewById(R.id.personName)
        phoneNoTextView = view.findViewById(R.id.phoneNo)

        // --- Setup Click Listeners ---
        view.findViewById<LinearLayout>(R.id.personalInfo).setOnClickListener {
            // Navigate to the detailed profile fragment
            findNavController().navigate(R.id.action_mainProfileFragment_to_profileFragment)
        }

        view.findViewById<LinearLayout>(R.id.yourOrdersLayout).setOnClickListener {
            // Navigate to the order history fragment
            findNavController().navigate(R.id.action_mainProfileFragment_to_orderSummary)
        }

        view.findViewById<LinearLayout>(R.id.logoutLayout).setOnClickListener {
            logoutUser()
        }

        view.findViewById<ImageView>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load user data into the UI
        loadUserData()
    }

    private fun loadUserData() {
        // Fetch cached data from SharedPreferences (saved from the dashboard)
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val phone = sharedPreferences.getString("phone_no", "N/A")

        personNameTextView.text = "$firstName $lastName"
        phoneNoTextView.text = phone
    }

    private fun logoutUser() {
        // 1. Clear all saved user data
        sharedPreferences.edit().clear().apply()

        // 2. Navigate to the Login screen using the action defined in the nav graph
        // This will also clear the back stack, so the user can't go back to the app.
        findNavController().navigate(R.id.action_mainProfileFragment_to_login)

        // 3. Finish the current activity to ensure the user is fully logged out.
        requireActivity().finish()
    }
}