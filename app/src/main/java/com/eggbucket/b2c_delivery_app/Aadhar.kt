package com.eggbucket.b2c_delivery_app

import android.Manifest
import android.app.Activity
import android.content.Context
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class Aadhar : AppCompatActivity() {

    private val PICK_FRONT_IMAGE_REQUEST_CODE = 1005
    private val PICK_BACK_IMAGE_REQUEST_CODE = 1006
    private val CAMERA_FRONT_REQUEST_CODE = 1010
    private val CAMERA_BACK_REQUEST_CODE = 1011
    private val CAMERA_PERMISSION_CODE = 2001

    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null
    private lateinit var tempFile: File
    private lateinit var loaderContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aadhar)

        val frontUploadButton: Button = findViewById(R.id.front_aadhar_upload_btn)
        val backUploadButton: Button = findViewById(R.id.back_aadhar_upload_btn)
        val submitButton: Button = findViewById(R.id.submit_aadhar_btn)
        val backButton: ImageView = findViewById(R.id.aadharBackButton)
        loaderContainer = findViewById(R.id.loaderContainer)

        backButton.setOnClickListener { finish() }

        frontUploadButton.setOnClickListener {
            showImageSourceDialog(PICK_FRONT_IMAGE_REQUEST_CODE, CAMERA_FRONT_REQUEST_CODE)
        }

        backUploadButton.setOnClickListener {
            showImageSourceDialog(PICK_BACK_IMAGE_REQUEST_CODE, CAMERA_BACK_REQUEST_CODE)
        }

        submitButton.setOnClickListener {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please upload both front and back images.", Toast.LENGTH_SHORT).show()
            } else {
                loaderContainer.visibility = android.view.View.VISIBLE
                val deliveryPartnerId = getSavedPhoneNumber()
                if (deliveryPartnerId != null) {
                    submitAadharDetails(deliveryPartnerId)
                } else {
                    loaderContainer.visibility = android.view.View.GONE // Hide progress bar
                    Toast.makeText(this, "Phone number not found. Please complete personal details first.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getSavedPhoneNumber(): String? {
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("phoneNumber", null)

    }

    private fun showImageSourceDialog(galleryRequestCode: Int, cameraRequestCode: Int) {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission(cameraRequestCode) // Camera
                1 -> pickImage(galleryRequestCode) // Gallery
            }
        }
        builder.show()
    }

    private fun checkCameraPermission(requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera(requestCode)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun openCamera(requestCode: Int) {
        try {
            tempFile = File.createTempFile("camera_image", ".jpg", cacheDir)
            val imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            }
            if (requestCode == CAMERA_FRONT_REQUEST_CODE) {
                startActivityForResult(intent, CAMERA_FRONT_REQUEST_CODE)
            } else if (requestCode == CAMERA_BACK_REQUEST_CODE) {
                startActivityForResult(intent, CAMERA_BACK_REQUEST_CODE)
            }
        } catch (e: Exception) {
            Log.e("Aadhar", "Error opening camera", e)
            Toast.makeText(this, "Failed to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        if (requestCode == PICK_FRONT_IMAGE_REQUEST_CODE) {
            startActivityForResult(intent, PICK_FRONT_IMAGE_REQUEST_CODE)
        } else if (requestCode == PICK_BACK_IMAGE_REQUEST_CODE) {
            startActivityForResult(intent, PICK_BACK_IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_FRONT_IMAGE_REQUEST_CODE -> {
                    frontImageUri = data?.data
                    updateFrontImageView(frontImageUri)
                }
                CAMERA_FRONT_REQUEST_CODE -> {
                    frontImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
                    updateFrontImageView(frontImageUri)
                }
                PICK_BACK_IMAGE_REQUEST_CODE -> {
                    backImageUri = data?.data
                    updateBackImageView(backImageUri)

                }
                CAMERA_BACK_REQUEST_CODE -> {
                    backImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
                    updateBackImageView(backImageUri)
                }
            }
        }
    }

    private fun updateBackImageView(imageUri: Uri?) {
        val imgBackAadhar: ImageView = findViewById(R.id.imgBackAadhar)
        val textView4: TextView = findViewById(R.id.textView4)
        val backUploadButton: Button = findViewById(R.id.back_aadhar_upload_btn)

        imgBackAadhar.setImageURI(imageUri)
        imgBackAadhar.visibility = android.view.View.VISIBLE

        textView4.visibility = android.view.View.GONE
        backUploadButton.visibility = android.view.View.GONE

    }

    private fun updateFrontImageView(imageUri: Uri?) {
        val imgFrontAadhar: ImageView = findViewById(R.id.imgFrontAadhar)
        val textView3: TextView = findViewById(R.id.textView3)
        val frontUploadButton: Button = findViewById(R.id.front_aadhar_upload_btn)


        imgFrontAadhar.setImageURI(imageUri)
        imgFrontAadhar.visibility = android.view.View.VISIBLE

        textView3.visibility = android.view.View.GONE
        frontUploadButton.visibility = android.view.View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (frontImageUri == null) {
                    openCamera(CAMERA_FRONT_REQUEST_CODE)
                } else {
                    openCamera(CAMERA_BACK_REQUEST_CODE)
                }
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }*/

    private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("compressed_image", ".jpg", cacheDir)

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val compressedBitmap = compressBitmap(bitmap, 400, 400) // Set dimensions

        FileOutputStream(tempFile).use { outputStream ->
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream) // Adjust quality
        }

        return tempFile
    }

    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height
        val width: Int
        val height: Int

        if (bitmap.width > bitmap.height) {
            width = maxWidth
            height = (maxWidth / aspectRatio).toInt()
        } else {
            height = maxHeight
            width = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    private fun submitAadharDetails(deliveryPartnerId: String) {
        try {
            frontImageUri?.let { frontUri ->
                backImageUri?.let { backUri ->
                    val frontFile = uriToFile(frontUri)
                    val backFile = uriToFile(backUri)

                    val frontRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), frontFile)
                    val backRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), backFile)

                    val frontPart = MultipartBody.Part.createFormData("front", frontFile.name, frontRequestBody)
                    val backPart = MultipartBody.Part.createFormData("back", backFile.name, backRequestBody)

                    val apiService = RetrofitClient.apiService

                    apiService.uploadAadharDetails(deliveryPartnerId, frontPart, backPart)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                loaderContainer.visibility = android.view.View.GONE
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
                                    val resultIntent = Intent().apply {
                                        putExtra("isAadharSubmitted", true) // Pass success status
                                    }
                                    setResult(Activity.RESULT_OK, resultIntent)
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
                                loaderContainer.visibility = android.view.View.GONE
                                Log.e("Aadhar", "Error uploading Aadhar", t)
                                Toast.makeText(this@Aadhar, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        } catch (e: Exception) {
            Log.e("Aadhar", "Error while processing Aadhar details", e)
            Toast.makeText(this, "Error processing Aadhar details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
