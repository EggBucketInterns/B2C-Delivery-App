package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide

class personalDetails : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profilePhoto: ImageView
    private lateinit var valueName: TextView
    private lateinit var valueFatherName: TextView
    private lateinit var valueDOB: TextView
    private lateinit var valuePhoneNumber: TextView
    private lateinit var valueSecondaryNumber: TextView
    private lateinit var valueBloodGroup: TextView
    private lateinit var valueCity: TextView
    private lateinit var valueAddress: TextView
    private lateinit var valueLanguageKnown: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_personal_details, container, false)

        view.setOnApplyWindowInsetsListener { v, insets ->
            val statusBarHeight = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                insets.getInsets(android.view.WindowInsets.Type.statusBars()).top
            } else {
                insets.systemWindowInsetTop
            }
            v.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        // Initialize Views
        profilePhoto = view.findViewById(R.id.profilePhoto)
        valueName = view.findViewById(R.id.valueName)
        valueFatherName = view.findViewById(R.id.valueFatherName)
        valueDOB = view.findViewById(R.id.valueDOB)
        valuePhoneNumber = view.findViewById(R.id.valuePhoneNumber)
        valueSecondaryNumber = view.findViewById(R.id.valueSecondaryNumber)
        valueBloodGroup = view.findViewById(R.id.valueBloodGroup)
        valueCity = view.findViewById(R.id.valueCity)
        valueAddress = view.findViewById(R.id.valueAddress)
        valueLanguageKnown = view.findViewById(R.id.valueLanguageKnown)

        // Get SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Fetch and set values
        valueName.text = "${sharedPreferences.getString("firstName", "")} ${sharedPreferences.getString("lastName", "")}"
        valueFatherName.text = sharedPreferences.getString("fatherName", "") ?: ""
        valueDOB.text = sharedPreferences.getString("dob", "") ?: ""
        valuePhoneNumber.text = sharedPreferences.getString("phone", "") ?: ""
        valueSecondaryNumber.text = sharedPreferences.getString("secondaryNumber", "") ?: ""
        valueBloodGroup.text = sharedPreferences.getString("bloodGroup", "") ?: ""
        valueCity.text = sharedPreferences.getString("city", "") ?: ""
        valueAddress.text = sharedPreferences.getString("address", "") ?: ""
        valueLanguageKnown.text = sharedPreferences.getString("languageKnown", "") ?: ""

        // Load profile image
        val imageUrl = sharedPreferences.getString("img", null)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person) // Fallback image
                .circleCrop()
                .into(profilePhoto)
        } else {
            profilePhoto.setImageResource(R.drawable.ic_person) // Default profile icon
        }

        // Back button click listener
        view.findViewById<ImageView>(R.id.aadharBackButton).setOnClickListener {
            NavHostFragment.findNavController(this@personalDetails).popBackStack()
        }

        return view
    }
}
