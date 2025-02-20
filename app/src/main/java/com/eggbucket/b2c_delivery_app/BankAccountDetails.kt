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
import android.widget.EditText
import android.widget.ImageView
//import android.widget.ProgressBar  // Uncomment if you add a ProgressBar in your layout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class BankAccountDetails : AppCompatActivity() {

    private var profileImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST_CODE = 1018
    private val CAMERA_REQUEST_CODE = 1010
    private val CAMERA_PERMISSION_CODE = 2001
    private lateinit var tempFile: File
    private lateinit var submitButton: Button
    // If using a ProgressBar, declare it here:
    // private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bank_account_details)

        // Retrieve views from XML
        val uploadImageButton: Button = findViewById(R.id.Upploadbutton)
        submitButton = findViewById(R.id.submitButtonBankDoc)
        // progressBar = findViewById(R.id.PIprogressBar)  // Uncomment if using a ProgressBar

        // Set up listener for image upload
        uploadImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        // Set up the submit button listener
        submitButton.setOnClickListener {
            Log.d("DEBUG", "Submit button clicked")
            Toast.makeText(this, "Submit clicked", Toast.LENGTH_SHORT).show()

            if (profileImageUri == null) {
                Toast.makeText(this, "Please upload an image before submitting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrieve user inputs from EditText fields
            val accHolderName = findViewById<EditText>(R.id.accountHolderNameInput).text.toString().trim()
            Log.d("DEBUG", "Account Holder Name : $accHolderName")
            val bankName = findViewById<EditText>(R.id.bankNameInput).text.toString().trim()
            val accNo = findViewById<EditText>(R.id.accountNumberInput).text.toString().trim()
            val branchName = findViewById<EditText>(R.id.branchNameInput).text.toString().trim()
            val ifscCode = findViewById<EditText>(R.id.ifscCodeInput).text.toString().trim()

            if (accHolderName.isNotBlank() && bankName.isNotBlank() && accNo.isNotBlank() && branchName.isNotBlank() && ifscCode.isNotBlank()) {
                // Call API to submit bank details.
                submitBankDetails(accHolderName, bankName, accNo, branchName, ifscCode)
                // Set result flag and finish the activity so that PersonalDocuments can update the UI.
                val resultIntent = Intent().apply {
                    putExtra("isBankDetailsSubmitted", true)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please fill all details correctly", Toast.LENGTH_SHORT).show()
            }
        }

        // Set window insets so that the layout doesn't get overlapped by system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bankDetails)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button listener to navigate back to PersonalDocuments.
        val prevButton: ImageView = findViewById(R.id.backArrowBank)
        prevButton.setOnClickListener {
            startActivity(Intent(this, PersonalDocuments::class.java))
        }
    }

    private fun submitBankDetails(accHolderName: String, bankName: String, accNo: String, branchName: String, ifscCode: String) {
        // Uncomment if using a ProgressBar:
        // progressBar.visibility = View.VISIBLE
        submitButton.visibility = View.INVISIBLE

        val deliveryPartnerId = getSavedPhoneNumber()
        Log.d("DEBUG", "Delivery Partner ID : $deliveryPartnerId")
        if (deliveryPartnerId.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(this, "Delivery Partner ID is missing", Toast.LENGTH_SHORT).show()
            }
            // progressBar.visibility = View.INVISIBLE
            submitButton.visibility = View.VISIBLE
            return
        }

        // Process the image: convert URI to File, compress it, etc.
        val imageFile = uriToFile(profileImageUri!!)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
        val compressedBitmap = compressBitmap(bitmap, 300, 300)
        val compressedFile = File(cacheDir, "compressed_image.jpg")
        FileOutputStream(compressedFile).use {
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
        }
        val imagePart = MultipartBody.Part.createFormData(
            "img",
            imageFile.name,
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
        )

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("accHolderName", accHolderName)
            .addFormDataPart("accNo", accNo)
            .addFormDataPart("bankName", bankName)
            .addFormDataPart("branchName", branchName)
            .addFormDataPart("ifscCode", ifscCode)
            .addPart(imagePart)
            .build()

        Log.d("DEBUG", "Request Body Parts: ${requestBody.parts}")

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/bankDetails/$deliveryPartnerId")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DEBUG", "Request failed: ${e.message}", e)
                runOnUiThread {
                    submitButton.visibility = View.VISIBLE
                    Toast.makeText(this@BankAccountDetails, "Submission failed", Toast.LENGTH_SHORT).show()
                    // progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DEBUG", "Response received")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("DEBUG", "Response Body: $responseBody")
                    runOnUiThread {
                        submitButton.visibility = View.VISIBLE
                        // progressBar.visibility = View.INVISIBLE
                        // Optionally, you could update the UI based on the response.
                    }
                } else {
                    runOnUiThread {
                        submitButton.visibility = View.VISIBLE
                        // progressBar.visibility = View.INVISIBLE
                        val errorBody = response.body?.string() ?: "No error body"
                        Log.e("DEBUG", "API Failure: Code: ${response.code}, Error Body: $errorBody")
                    }
                }
            }
        })
    }

    private fun getSavedPhoneNumber(): String? {
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        // Use the key "phoneNumber" consistently
        return sharedPreferences.getString("phoneNumber", null)
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

    private fun uriToFile(uri: Uri): File {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

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
            val imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            }
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e("BankAccountDetails", "Error opening camera", e)
            Toast.makeText(this, "Failed to open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val imageView: ImageView = findViewById(R.id.imgPassbookDetails)
            val uploadButton: Button = findViewById(R.id.Upploadbutton)
            val textView: TextView = findViewById(R.id.textView3)
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    profileImageUri = data?.data
                    imageView.setImageURI(profileImageUri)
                    Toast.makeText(this, "Image selected successfully.", Toast.LENGTH_SHORT).show()
                }
                CAMERA_REQUEST_CODE -> {
                    profileImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
                    imageView.setImageURI(profileImageUri)
                    Toast.makeText(this, "Photo taken successfully.", Toast.LENGTH_SHORT).show()
                }
            }
            textView.visibility = View.GONE
            uploadButton.visibility = View.GONE
            imageView.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "No image selected or photo taken.", Toast.LENGTH_SHORT).show()
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
}
