package com.eggbucket.b2c_delivery_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class PersonalInformation : AppCompatActivity() {

    private var profileImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1010
    private val CAMERA_PERMISSION_CODE = 2001
    private lateinit var tempFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        val uploadImageButton: Button = findViewById(R.id.Upploadbutton)
        val submitButton: Button = findViewById(R.id.submit_vehicle_button)
        val dateOfBirthInput: EditText = findViewById(R.id.dateOfBirthInput)
        uploadImageButton.setOnClickListener {
            showImageSourceDialog()
        }
        submitButton.setOnClickListener {
            submitPersonalDetails()
        }
        dateOfBirthInput.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show the DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date as dd-mm-yyyy
                    val date = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                    // Set the formatted date in the EditText
                    dateOfBirthInput.setText(date)
                },
                year,
                month,
                day
            )

            // Show the dialog
            datePickerDialog.show()
        }
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
            Log.e("PersonalInformation", "Error opening camera", e)
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
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> {
                    profileImageUri = data?.data
                    Toast.makeText(this, "Image selected successfully.", Toast.LENGTH_SHORT).show()
                }
                CAMERA_REQUEST_CODE -> {
                    profileImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
                    Toast.makeText(this, "Photo taken successfully.", Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun submitPersonalDetails() {
        startActivity(Intent(this, PersonalDocuments::class.java))
        finish()
    }
}

/*
val firstName = findViewById<EditText>(R.id.firstNameInput).text.toString().trim()
val lastName = findViewById<EditText>(R.id.lastNameInput).text.toString().trim()
val fatherName = findViewById<EditText>(R.id.fatherNameInput).text.toString().trim()
val dob = findViewById<EditText>(R.id.dateOfBirthInput).text.toString().trim()
val phone = findViewById<EditText>(R.id.primaryPhoneInput).text.toString().trim()
val secondaryNumber = findViewById<EditText>(R.id.secondaryPhoneInput).text.toString().trim()
val bloodGroup = findViewById<EditText>(R.id.bloodGroupInput).text.toString().trim()
val city = findViewById<EditText>(R.id.cityInput).text.toString().trim()
val address = findViewById<EditText>(R.id.addressInput).text.toString().trim()
val languageKnown = findViewById<EditText>(R.id.languagesInput).text.toString().trim()

if (profileImageUri == null || firstName.isEmpty() || phone.isEmpty()) {
    Toast.makeText(this, "Profile image, first name, and phone are required.", Toast.LENGTH_SHORT).show()
    return
}

val imageFile = uriToFile(profileImageUri!!)
val imagePart = MultipartBody.Part.createFormData(
    "img", imageFile.name, RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
)

val api = RetrofitClient.apiService
val call = api.submitPersonalDetails(
    firstName.toRequestBody(), lastName.toRequestBody(), fatherName.toRequestBody(),
    dob.toRequestBody(), phone.toRequestBody(), secondaryNumber.toRequestBody(),
    bloodGroup.toRequestBody(), city.toRequestBody(), address.toRequestBody(),
    languageKnown.toRequestBody(), imagePart
)

call.enqueue(
    onResponse = { response ->
        if (response.isSuccessful) {
            Toast.makeText(this, "Submitted successfully.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PersonalDocuments::class.java))
            finish()
        } else {
            Log.e("API Error", "Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
            Toast.makeText(this, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
        }
    },
    onFailure = { throwable ->
        Log.e("API Failure", throwable.message.toString())
        Toast.makeText(this, "Network error: ${throwable.message}", Toast.LENGTH_LONG).show()
    }
)


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

private fun String.toRequestBody(): RequestBody =
RequestBody.create("text/plain".toMediaTypeOrNull(), this)

override fun onSaveInstanceState(outState: Bundle) {
super.onSaveInstanceState(outState)
outState.putParcelable("profileImageUri", profileImageUri)
}
}
*/