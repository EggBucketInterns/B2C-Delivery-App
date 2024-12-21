package com.eggbucket.b2c_delivery_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
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
import java.util.Calendar
import java.util.concurrent.TimeUnit

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
            val firstName = findViewById<EditText>(R.id.firstNameInput).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.lastNameInput).text.toString().trim()
            val fatherName = findViewById<EditText>(R.id.fatherNameInput).text.toString().trim()
            val dob = findViewById<EditText>(R.id.dateOfBirthInput).text.toString().trim()
            val phone = findViewById<EditText>(R.id.primaryPhoneInput).text.toString().trim()
            val secondaryNumber = findViewById<EditText>(R.id.secondaryPhoneInput).text.toString().trim()
            val bloodGroup = findViewById<EditText>(R.id.bloodGroupInput).text.toString().trim()
            val city = findViewById<EditText>(R.id.cityInput1).text.toString().trim()
            val languageKnown = findViewById<EditText>(R.id.languagesInput).text.toString().trim()
            savePhoneNumber(phone)
            submitPersonalDetails(firstName,lastName,fatherName,dob,phone,secondaryNumber,bloodGroup,city,languageKnown)
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
    private fun savePhoneNumber(phone: String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("phoneNumber", phone)
        editor.apply()
    }

    private fun submitPersonalDetails(firstName:String,
                                      lastName:String,
                                      fatherName:String,
                                      dob:String,
                                      phone:String,
                                      secondaryNumber:String,
                                      bloodGroup:String,
                                      city:String,
                                      languageKnown:String) {
        val addressJson = """
        {
            "coordinates": {
                "lat": 12.9494,
                "long": 77.5847
            },
            "fullAddress": {
                "addressLine1": "011",
                "addressLine2": "the",
                "area": "basavanagudi",
                "city": "bang",
                "country": "ind",
                "flatNo": "101",
                "state": "kar",
                "zipCode": "456372"
            }
        }
    """.trimIndent()
        val imageFile = uriToFile(profileImageUri!!) // Assuming this function works properly


        val imagePart = MultipartBody.Part.createFormData(
            "img", // Field name for the image
            imageFile.name, // File name
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile) // RequestBody for the image file
        )
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("firstName", firstName)
            .addFormDataPart("lastName", lastName)
            .addFormDataPart("fatherName", fatherName)
            .addFormDataPart("dob",dob ) // Example date of birth
            .addFormDataPart("phone", phone)
            .addFormDataPart("secondaryNumber", secondaryNumber)
            .addFormDataPart("bloodGroup", bloodGroup)
            .addFormDataPart("city", city)
            .addFormDataPart("address", addressJson) // Send address as a JSON string
            .addFormDataPart("languageKnown", languageKnown)
            .addPart(imagePart) // Attach the image part here
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Set connect timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
            .build()

// Create the request
        val request = Request.Builder()
            .url("https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/personalInformation")
            .post(requestBody)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                println("Response code: ${response.code}")
                if (response.isSuccessful) {
                    startActivity(Intent(this@PersonalInformation, PersonalDocuments::class.java))
                    finish()
                }
                else {
                    println("Request failed: ${response.message}")
                    Toast.makeText(this@PersonalInformation, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        startActivity(Intent(this, PersonalDocuments::class.java))

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