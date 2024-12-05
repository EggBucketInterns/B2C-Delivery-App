package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.eggbucket.b2c_delivery_app.databinding.FragmentProfileBinding

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient


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
data class User(
    val generalDetails: GeneralDetails? = null,
)

data class GeneralDetails(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val image: String? = null,
    val bloodGroup: String? = null,
    val fatherName: String? = null,
    val secondaryNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val dob: Dob? = null,
    val languageKnown: List<String>? = null
)

data class Dob(
    val _seconds: Long,
    val _nanoseconds: Int
)





class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // SharedPreferences
        sharedPref = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)



        val phoneNumber = sharedPref.getString("phone", null)


        phoneNumber?.let {
            fetchUserData(it)
        }

        setupClickListeners()
        return binding.root
    }

    private fun fetchUserData(phone: String) {
        // API call using Retrofit
        Log.d("ProfileFragment", "Request URL: https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/profile/$phone")
        Log.d("ProfileFragment", "Formatted phone number: $phone")

        RetrofitClient.apiService.getUserByPhone(phone).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        val generalDetails = it.generalDetails
                        generalDetails?.let { details ->
                            // Update UI
                            binding.tvPartnerName.text = "${details.firstName} ${details.lastName}"
                            binding.tvSubtitle.text = details.phone

                            // Update SharedPreferences with fetched user data
                            val editor = sharedPref.edit()
                            editor.putString("firstName", details.firstName)
                            editor.putString("lastName", details.lastName)
                            editor.putString("phone", details.phone)
                            editor.putString("image", details.image)
                            editor.apply()

                            details.image?.let { imageUrl ->
                                Glide.with(this@ProfileFragment)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.img) // Placeholder image
                                    .into(binding.profileImage)
                            }
                        }
                    }
                    Log.d("ProfileFragment", "User Data: ${response.body()}")
                } else {
                    // Handle error response
                    Log.e("ProfileFragment", "API call failed with response code: ${response.code()}")
                    Log.e("ProfileFragment", "Response message: ${response.message()}")
                }
            }


            override fun onFailure(call: Call<User>, t: Throwable) {
                // Handle failure (e.g., no internet connection)
                Log.e("ProfileFragment", "API call failed", t)
                Log.e("ProfileFragment", "Failure reason: ${t.localizedMessage}")
            }
        })
    }

    private fun setupClickListeners() {
        // Navigate to Personal Information
        binding.llPersonalInfo.setOnClickListener {
            val intent = Intent(requireContext(), PersonalInformation::class.java)
            startActivity(intent)
        }

        // Navigate to Order History
        binding.llOrderHistory.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_orderSummary)
        }

        // Navigate to Notifications
        binding.llNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_newOrder)
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
        // Clear user details from SharedPreferences
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        // Redirect to Login Activity
        val intent = Intent(requireActivity(), Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



