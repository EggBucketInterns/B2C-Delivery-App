package com.eggbucket.b2c_delivery_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
            onBackPressed()
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
                submitPANDetails()
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

    private fun submitPANDetails() {
        if (frontImageUri == null || backImageUri == null) {
            Toast.makeText(this, "Please select both front and back images.", Toast.LENGTH_SHORT).show()
            return
        }

        val frontImageFile = uriToFile(frontImageUri!!)
        val backImageFile = uriToFile(backImageUri!!)

        val frontRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), frontImageFile)
        val backRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), backImageFile)

        val frontImagePart = MultipartBody.Part.createFormData("frontImage", frontImageFile.name, frontRequestBody)
        val backImagePart = MultipartBody.Part.createFormData("backImage", backImageFile.name, backRequestBody)

        val api = RetrofitClient.retrofit.create(ApiService::class.java)
        api.uploadPanDetails(frontImagePart, backImagePart).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        Toast.makeText(this@PanCard, responseBody.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PanCard, "Unexpected response format.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PanCard, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PanCard, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val filePath = uri.path ?: throw IllegalArgumentException("Invalid URI")
        return File(filePath)
    }
}