package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eggbucket.b2c_delivery_app.R
import com.eggbucket.b2c_delivery_app.ResponseBody
import com.eggbucket.b2c_delivery_app.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class VehicleDetails : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST_CODE = 1001
    private var vehicleDocUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)

        // Button to select image from gallery
        val uploadButton: Button = findViewById(R.id.Upploadbutton)
        uploadButton.setOnClickListener {
            pickImage()
        }

        val backButton: Button = findViewById(R.id.vehicledetailsBackBtn)
        backButton.setOnClickListener {
            finish() // Finish the activity and go back to the previous screen
        }

        // Submit button to upload image
        val submitButton: Button = findViewById(R.id.submit_vehicle_button)
        submitButton.setOnClickListener {
            vehicleDocUri?.let {
                uploadVehicleDetails("12345") // Replace with actual delivery partner ID
            } ?: run {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to open gallery and pick an image
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            vehicleDocUri = data.data
        }
    }

    // Convert URI to a File object
    private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

    // Function to upload vehicle details with the selected image
    private fun uploadVehicleDetails(deliveryPartnerId: String) {
        try {
            vehicleDocUri?.let {
                val vehicleDocFile = uriToFile(it) // Convert URI to File
                val vehicleDocRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), vehicleDocFile)
                val vehicleDocPart = MultipartBody.Part.createFormData("img", vehicleDocFile.name, vehicleDocRequestBody)

                // Get the Retrofit service
                val apiService = RetrofitClient.apiService

                // Call the API to upload the image
                apiService.uploadVehicleDocument(deliveryPartnerId, vehicleDocPart).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val uploadResponse = response.body()
                            Toast.makeText(this@VehicleDetails, uploadResponse?.message ?: "Upload successful", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@VehicleDetails, "Upload failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("VehicleDetails", "Error uploading vehicle document", t)
                        Toast.makeText(this@VehicleDetails, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("VehicleDetails", "Error while converting URI to file", e)
            Toast.makeText(this, "Error while converting URI to file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
