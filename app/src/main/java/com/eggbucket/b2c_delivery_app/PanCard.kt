package com.eggbucket.b2c_delivery_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class PanCard : AppCompatActivity() {

    private val PICK_FRONT_IMAGE_REQUEST_CODE = 1001
    private val PICK_BACK_IMAGE_REQUEST_CODE = 1002

    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pan_card)
        // Use the same layout file
        val linearlayout = findViewById<View>(R.id.linearlayoutpan)
        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()

        val frontPanUploadButton: Button = findViewById(R.id.front_pan_upload_button)
        val backPanUploadButton: Button = findViewById(R.id.back_pan_upload_button)

        val submitPanButton: Button = findViewById(R.id.submit_pan_button)
        val panBackButton: Button = findViewById(R.id.panBackBtn) // Might need to change to ImageButton

        // Handle back navigation
        panBackButton.setOnClickListener {
            finish()
        }

        // Handle upload button click
        frontPanUploadButton.setOnClickListener {
            pickImage(PICK_FRONT_IMAGE_REQUEST_CODE)
        }

        backPanUploadButton.setOnClickListener{
            pickImage(PICK_BACK_IMAGE_REQUEST_CODE)
        }

        // Handle submit button click (needs implementation based on your logic)
        submitPanButton.setOnClickListener {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please upload both front and back images.", Toast.LENGTH_SHORT).show()
            } else {
                submitPANDetails("12345")
            }
        }
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data

            when (requestCode) {
                PICK_FRONT_IMAGE_REQUEST_CODE -> {
                    frontImageUri = selectedImageUri
                    Toast.makeText(this, "Front image selected.", Toast.LENGTH_SHORT).show()
                }
                PICK_BACK_IMAGE_REQUEST_CODE -> {
                    backImageUri = selectedImageUri
                    Toast.makeText(this, "Back image selected.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitPANDetails(deliveryPartnerId: String) {
        try {
            // Ensure both images are selected
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please select both front and back images.", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert URIs to files
            val frontImageFile = uriToFile(frontImageUri!!)
            val backImageFile = uriToFile(backImageUri!!)

            // Create request bodies and multipart parts
            val frontRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), frontImageFile)
            val backRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), backImageFile)

            val frontImagePart = MultipartBody.Part.createFormData("front", frontImageFile.name, frontRequestBody)
            val backImagePart = MultipartBody.Part.createFormData("back", backImageFile.name, backRequestBody)

            // Get the Retrofit service
            val apiService = RetrofitClient.apiService

            // Call the API to upload PAN details
            apiService.uploadPanDetails(deliveryPartnerId, frontImagePart, backImagePart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                Toast.makeText(
                                    this@PanCard,
                                    responseBody.message ?: "PAN uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this@PanCard, "Unexpected response format.", Toast.LENGTH_SHORT).show()
                            }
                            val resultIntent = Intent().apply {
                                putExtra("isPanCardSubmitted", true) // Pass success status
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("PanCardDetails", "Upload failed: ${response.code()}, Message: $errorBody")
                            Toast.makeText(
                                this@PanCard,
                                "Failed to upload PAN: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("PanCardDetails", "Error uploading PAN document", t)
                        Toast.makeText(
                            this@PanCard,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            Log.e("PanCardDetails", "Error while processing PAN details", e)
            Toast.makeText(
                this,
                "Error processing PAN details: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }
}