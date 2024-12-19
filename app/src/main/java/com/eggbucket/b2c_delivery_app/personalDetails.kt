package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget

import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


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
    private lateinit var imageview:ImageView
    private var fileName = "profile_image.jpg"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_details, container, false)

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
        imageview = view.findViewById(R.id.profilePhoto)
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        loadData()

        return view
    }

    private fun loadData() {
        val isDataStored = sharedPreferences.getBoolean("isDataStored", false)
        if (isDataStored) {
            displayFromPreferences()
            displayImageFromLocal(imageview, fileName)
        } else {
            fetchFromApiAndStore()
        }
    }

    private fun displayFromPreferences() {
        Log.d("ProfileData", "Loading data from preferences")

        valueName.text = "${sharedPreferences.getString("firstName", "")} ${sharedPreferences.getString("lastName", "")}"
        valueFatherName.text = sharedPreferences.getString("fatherName", "")
        valueDOB.text = sharedPreferences.getString("dob", "")
        valuePhoneNumber.text = sharedPreferences.getString("phone", "")
        valueSecondaryNumber.text = sharedPreferences.getString("secondaryNumber", "")
        valueBloodGroup.text = sharedPreferences.getString("bloodGroup", "")
        valueCity.text = sharedPreferences.getString("city", "")
        valueAddress.text = sharedPreferences.getString("address", "")
        valueLanguageKnown.text = sharedPreferences.getString("languageKnown", "")


    }


    private fun fetchFromApiAndStore() {
        val driverId = "888" // Replace with the actual driver ID

        lifecycleScope.launch {
            try {
                // Call the API using suspend function
                val apiResponse = RetrofitClient.apiService.getGeneralDetails(driverId)

                // Extract general details
                val generalDetails = apiResponse.generalDetails
                Log.d("API_SUCCESS", "General details: $generalDetails")

                // Convert Timestamp to string (example format: YYYY-MM-DD)
                val dob = generalDetails.dob?.let {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(it._seconds * 1000))
                }.orEmpty()

                // Save to SharedPreferences
                saveToPreferences(
                    generalDetails.firstName.orEmpty(),
                    generalDetails.lastName.orEmpty(),
                    generalDetails.fatherName.orEmpty(),
                    dob,
                    generalDetails.phone.orEmpty(),
                    generalDetails.secondaryNumber.orEmpty(),
                    generalDetails.bloodGroup.orEmpty(),
                    generalDetails.city.orEmpty(),
                    generalDetails.address.orEmpty(),
                    generalDetails.languageKnown.joinToString(", ").orEmpty(),
                    generalDetails.image.orEmpty()
                )
                displayFromPreferences()
                if(generalDetails.image.isNotEmpty()){
                    val imageUrl = generalDetails.image
                    val imageFile = downloadAndStoreImage(imageUrl, fileName)
                    if (imageFile != null) {
                        displayImageFromLocal(imageview, fileName)

                    } else {
                        Log.e("PROFILE_IMAGE", "Failed to store or load image")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error occurred: ${e.message}", e)
            }
        }
    }

    private fun saveToPreferences(
        firstName: String, lastName: String, fatherName: String, dob: String,
        phone: String, secondaryNumber: String, bloodGroup: String, city: String,
        address: String, languageKnown: String, img: String
    ) {
        sharedPreferences.edit().apply {
            putBoolean("isDataStored", true)
            putString("firstName", firstName)
            putString("lastName", lastName)
            putString("fatherName", fatherName)
            putString("dob", dob)
            putString("phone", phone)
            putString("secondaryNumber", secondaryNumber)
            putString("bloodGroup", bloodGroup)
            putString("city", city)
            putString("address", address)
            putString("languageKnown", languageKnown)
            putString("img", img)
            apply()
        }
    }
    private fun downloadAndStoreImage(imageUrl: String, fileName: String): File? {
        return try {
            val file = File(context?.filesDir ?: null, fileName) // Store in app-specific files directory
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        // Save the bitmap to the file
                        FileOutputStream(file).use { outputStream ->
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle placeholder if necessary
                    }
                })
            file
        } catch (e: Exception) {
            Log.e("IMAGE_STORE", "Failed to store image: ${e.message}", e)
            null
        }
    }
    private fun displayImageFromLocal(imageView: ImageView, fileName: String) {
        val file = File(context?.filesDir, fileName) // Get the file from app's internal storage
        if (file.exists()) {
            Glide.with(this)
                .load(file) // Load the image file into the ImageView
                .placeholder(R.drawable.ic_person) // Fallback image
                .into(imageView)
        } else {
            Log.e("IMAGE_DISPLAY", "File does not exist: $fileName")
        }
    }


}
