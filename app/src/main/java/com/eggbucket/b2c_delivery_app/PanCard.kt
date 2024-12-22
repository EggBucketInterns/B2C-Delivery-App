package com.eggbucket.b2c_delivery_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

class PanCard : AppCompatActivity() {

    private val PICK_FRONT_IMAGE_REQUEST_CODE = 1001
    private val PICK_BACK_IMAGE_REQUEST_CODE = 1002
    private val CAMERA_FRONT_REQUEST_CODE = 1010
    private val CAMERA_BACK_REQUEST_CODE = 1011
    private val CAMERA_PERMISSION_CODE = 2001

    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null
    private lateinit var tempFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pan_card)

        val frontPanUploadButton: Button = findViewById(R.id.front_pan_upload_button)
        val backPanUploadButton: Button = findViewById(R.id.back_pan_upload_button)
        val submitPanButton: Button = findViewById(R.id.submit_pan_button)
        val panBackButton: ImageView = findViewById(R.id.panBackBtn)

        val loaderContainer: View = findViewById(R.id.loaderContainer)

        panBackButton.setOnClickListener {
            finish()
        }

        frontPanUploadButton.setOnClickListener {
            showImageSourceDialog(PICK_FRONT_IMAGE_REQUEST_CODE, CAMERA_FRONT_REQUEST_CODE)
        }

        backPanUploadButton.setOnClickListener {
            showImageSourceDialog(PICK_BACK_IMAGE_REQUEST_CODE, CAMERA_BACK_REQUEST_CODE)
        }

        submitPanButton.setOnClickListener {
            if (frontImageUri == null || backImageUri == null) {
                Toast.makeText(this, "Please upload both front and back images.", Toast.LENGTH_SHORT).show()
            } else {
                loaderContainer.visibility = View.VISIBLE
                val deliveryPartnerId = getSavedPhoneNumber()
                if (deliveryPartnerId != null) {
                    submitPANDetails(deliveryPartnerId) { success ->
                        loaderContainer.visibility = View.GONE
                        if (success) {
                            val resultIntent = Intent().apply {
                                putExtra("isPanCardSubmitted", true)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    }
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
            Log.e("PanCard", "Error opening camera", e)
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
        val imgBackPan: ImageView = findViewById(R.id.imgBackPan)
        val tvb: TextView = findViewById(R.id.back_pan_text_view)
        val backUploadButton: Button = findViewById(R.id.back_pan_upload_button)

        imgBackPan.setImageURI(imageUri)
        imgBackPan.visibility = android.view.View.VISIBLE

        tvb.visibility = android.view.View.GONE
        backUploadButton.visibility = android.view.View.GONE
    }

    private fun updateFrontImageView(imageUri: Uri?) {
        val imgFrontPan: ImageView = findViewById(R.id.imgFrontPan)
        val tvf: TextView = findViewById(R.id.frontPanTextView)
        val frontUploadButton: Button = findViewById(R.id.front_pan_upload_button)


        imgFrontPan.setImageURI(imageUri)
        imgFrontPan.visibility = android.view.View.VISIBLE

        tvf.visibility = android.view.View.GONE
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

   /* private fun uriToFile(uri: Uri): File {
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

    private fun submitPANDetails(deliveryPartnerId: String, callback: (Boolean) -> Unit) {
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

                    apiService.uploadPanDetails(deliveryPartnerId, frontPart, backPart)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                callback(response.isSuccessful)
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Log.e("PanCard", "Error uploading PAN", t)
                                callback(false)
                            }
                        })
                }
            }
        } catch (e: Exception) {
            Log.e("PanCard", "Error while processing PAN details", e)
            callback(false)
        }
    }
}
