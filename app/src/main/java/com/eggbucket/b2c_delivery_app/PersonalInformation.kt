package com.eggbucket.b2c_delivery_app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class PersonalInformation : AppCompatActivity() {

    private var profileImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST_CODE = 1001

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        if (savedInstanceState != null) {
            profileImageUri = savedInstanceState.getParcelable("profileImageUri")
        }

        val scrollView = findViewById<View>(R.id.scrollPersonal)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val uploadImageButton: Button = findViewById(R.id.Upploadbutton)
        val submitButton: Button = findViewById(R.id.submit_vehicle_button)

        uploadImageButton.setOnClickListener { pickImage() }
        submitButton.setOnClickListener { submitPersonalDetails() }
    }

    private fun pickImage() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            profileImageUri = data.data
            Toast.makeText(this, "Image selected successfully.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show()
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