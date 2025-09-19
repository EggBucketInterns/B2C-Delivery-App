package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.eggbucket.b2c_delivery_app.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth


/*object RetrofitClient {
    private const val BASE_URL = "https://b2c-backend-1.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}*/


/*interface ApiService {

    @GET("api/v1/deliveryPartner/profile/{phone}")
    fun getUserByPhone(@Path("phone") phone: String): Call<User>
}*/



data class Dob(
    val _seconds: Long,
    val _nanoseconds: Int
)





class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.root.setOnApplyWindowInsetsListener { v, insets ->
            val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.statusBars()).top
            } else {
                insets.systemWindowInsetTop
            }
            v.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        // Initialize SharedPreferences
        sharedPref = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        auth = FirebaseAuth.getInstance()
        // Load user data from SharedPreferences
        loadUserDataFromPreferences()

        // Setup click listeners
        setupClickListeners()

        return binding.root
    }


    private fun loadUserDataFromPreferences() {
        val firstName = sharedPref.getString("firstName", "N/A")
        val lastName = sharedPref.getString("lastName", "N/A")
        val phone = sharedPref.getString("phone", "N/A")
        val image = sharedPref.getString("img", null)

        // Update UI with user data
        binding.tvPartnerName.text = "$firstName $lastName"
        binding.tvSubtitle.text = phone

        Log.d("ProfileFragment", "Details: $firstName" + lastName + phone + image )

        // Load profile image using Glide
        Glide.with(this)
            .load(image) // URL or path to the new image
            .placeholder(R.drawable.ic_person) // Placeholder image
            .circleCrop() // Ensure circular cropping
            .into(binding.profileImage) // Target ImageView
    }

    private fun setupClickListeners() {
        // Navigate to Personal Information
        binding.llPersonalInfo.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_personalDetails)
        }

        // Navigate to Order History
        binding.llOrderHistory.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_orderSummary)
        }

        // Navigate to Notifications
        binding.llNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationFragment)
        }

        // Handle Logout
        binding.llLogout.setOnClickListener {
            logout()
        }

        binding.llHelpSupport.setOnClickListener {
            // Handle Help and Support button click
        }




    }

    private fun logout() {
        // FIX 1: Sign out from Firebase Authentication first
        auth.signOut()

        // Clear user details from SharedPreferences
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        // FIX 2: Redirect to the correct LoginActivity
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




