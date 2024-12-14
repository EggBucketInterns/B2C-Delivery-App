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


class Aadhar : AppCompatActivity() {

    private val PICK_FRONT_IMAGE_REQUEST_CODE = 1005
    private val PICK_BACK_IMAGE_REQUEST_CODE = 1006
    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aadhar)

        val linearlayout = findViewById<View>(R.id.linear_layout_aadhar)
        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()

        val frontUploadButton: Button = findViewById(R.id.front_aadhar_upload_btn)
        val backUploadButton: Button = findViewById(R.id.back_aadhar_upload_btn)
        val submitButton: Button = findViewById(R.id.submit_aadhar_btn)
        val backButton: Button = findViewById(R.id.aadharBackButton)

        backButton.setOnClickListener { finish() }

        frontUploadButton.setOnClickListener { pickImage(PICK_FRONT_IMAGE_REQUEST_CODE) }
        backUploadButton.setOnClickListener { pickImage(PICK_BACK_IMAGE_REQUEST_CODE) }

        submitButton.setOnClickListener {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please upload both front and back images.", Toast.LENGTH_SHORT).show()
            } else {
                submitAadharDetails("12345") // Replace with actual delivery partner ID
            }
        }
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
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

    private fun submitAadharDetails(deliveryPartnerId: String) {
        try {
            // Ensure both images are selected
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please select both front and back images.", Toast.LENGTH_SHORT).show()
                return
            }

            // Convert URIs to files
            val frontImageFile = uriToFile(frontImageUri!!)
            val backImageFile = uriToFile(backImageUri!!)

            // Create request bodies and parts
            val frontRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), frontImageFile)
            val backRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), backImageFile)

            val frontImagePart = MultipartBody.Part.createFormData("front", frontImageFile.name, frontRequestBody)
            val backImagePart = MultipartBody.Part.createFormData("back", backImageFile.name, backRequestBody)

            // Get the Retrofit service
            val apiService = RetrofitClient.apiService

            // Call the API to upload the images
            apiService.uploadAadharDetails(deliveryPartnerId, frontImagePart, backImagePart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                Toast.makeText(
                                    this@Aadhar,
                                    responseBody.message ?: "PAN uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this@Aadhar, "Unexpected response format.", Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("AadharDetails", "Upload failed: ${response.code()}, Message: $errorBody")

                            // Show appropriate message to the user
                            Toast.makeText(
                                this@Aadhar,
                                "Failed to upload Aadhar: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("AadharDetails", "Error uploading Aadhar document", t)
                        Toast.makeText(
                            this@Aadhar,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            Log.e("AadharDetails", "Error while processing Aadhar details", e)
            Toast.makeText(
                this,
                "Error processing Aadhar details: ${e.message}",
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
