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

class DrivingLicense : AppCompatActivity() {

    private val PICK_FRONT_IMAGE_REQUEST_CODE = 1001
    private val PICK_BACK_IMAGE_REQUEST_CODE = 1002

    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving_license)

        // Set up layout insets
        val linearLayout = findViewById<View>(R.id.linearLayoutDL)
        ViewCompat.setOnApplyWindowInsetsListener(linearLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()

        // Initialize buttons
        val frontDLUploadButton: Button = findViewById(R.id.front_DL_upload_button)
        val backDLUploadButton: Button = findViewById(R.id.back_DL_upload_button)
        val submitDLButton: Button = findViewById(R.id.submit_DL_btn)
        val dlBackButton: Button = findViewById(R.id.DlBackBtn)

        // Back navigation
        dlBackButton.setOnClickListener {
            finish()
        }

        // Image selection
        frontDLUploadButton.setOnClickListener {
            pickImage(PICK_FRONT_IMAGE_REQUEST_CODE)
        }

        backDLUploadButton.setOnClickListener {
            pickImage(PICK_BACK_IMAGE_REQUEST_CODE)
        }

        // Submit DL details
        submitDLButton.setOnClickListener {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please upload both front and back images.", Toast.LENGTH_SHORT).show()
            } else {
                submitDLDetails("12345") // Pass actual delivery partner ID
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

    private fun submitDLDetails(deliveryPartnerId: String) {
        try {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please select both front and back images.", Toast.LENGTH_SHORT).show()
                return
            }

            val frontImageFile = uriToFile(frontImageUri!!)
            val backImageFile = uriToFile(backImageUri!!)

            val frontRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), frontImageFile)
            val backRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), backImageFile)

            val frontImagePart = MultipartBody.Part.createFormData("front", frontImageFile.name, frontRequestBody)
            val backImagePart = MultipartBody.Part.createFormData("back", backImageFile.name, backRequestBody)

            val apiService = RetrofitClient.apiService

            apiService.uploadDLDetails(deliveryPartnerId, frontImagePart, backImagePart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                Toast.makeText(
                                    this@DrivingLicense,
                                    responseBody.message ?: "Driving License uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()}else {
                                    Toast.makeText(this@DrivingLicense, "Unexpected response format.", Toast.LENGTH_SHORT).show()
                                }
                            val resultIntent = Intent().apply {
                                putExtra("isDrivingLicenseSubmitted", true) // Pass success status
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("DrivingLicenseDetails", "Upload failed: ${response.code()}, Message: $errorBody")
                            Toast.makeText(
                                this@DrivingLicense,
                                "Failed to upload DL: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("DrivingLicenseDetails", "Error uploading DL document", t)
                        Toast.makeText(
                            this@DrivingLicense,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

        } catch (e: Exception) {
            Log.e("DrivingLicenseDetails", "Error while processing DL details", e)
            Toast.makeText(
                this,
                "Error processing DL details: ${e.message}",
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
