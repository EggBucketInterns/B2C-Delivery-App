package com.eggbucket.b2c_delivery_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import android.util.Log

class VehicleDetails : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST_CODE = 1001
    private val PICK_CAMERA_REQUEST_CODE = 1002
    private val CAMERA_PERMISSION_CODE = 2001

    private var vehicleDocUri: Uri? = null
    private lateinit var tempFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_details)
        //Photo or Camera
        val uploadButton: Button = findViewById(R.id.Upploadbutton)
        uploadButton.setOnClickListener {
            showImageSourceDialog()
        }

        val backButton: Button = findViewById(R.id.vehicledetailsBackBtn)
        backButton.setOnClickListener {
            finish()
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

    // gallery or camera
    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission() // Camera
                1 -> pickImage() // Gallery
            }
        }
        builder.show()
    }
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }
    private fun openCamera() {
        try {
            tempFile = File.createTempFile("camera_image", ".jpg", cacheDir)

            vehicleDocUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, vehicleDocUri)
            }
            startActivityForResult(intent, PICK_CAMERA_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e("VehicleDetails", "Error opening camera", e)
            Toast.makeText(this, "Failed to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    vehicleDocUri = data?.data
                }
                PICK_CAMERA_REQUEST_CODE -> {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
            }
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

    private fun uploadVehicleDetails(deliveryPartnerId: String) {
        try {
            vehicleDocUri?.let {
                val vehicleDocFile = uriToFile(it)
                val vehicleDocRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), vehicleDocFile)
                val vehicleDocPart = MultipartBody.Part.createFormData("img", vehicleDocFile.name, vehicleDocRequestBody)

                val apiService = RetrofitClient.apiService

                // Call the API to upload the image
                apiService.uploadVehicleDocument(deliveryPartnerId, vehicleDocPart).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VehicleDetails, "Upload successful", Toast.LENGTH_SHORT).show()

                            // Send result back to PersonalDocuments activity to update the status
                            val resultIntent = Intent().apply {
                                putExtra("isVehicleDetailsSubmitted", true)
                            }
                            setResult(RESULT_OK, resultIntent)
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
