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
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

            if (validateFields()&&profileImageUri != null && uriToFile(profileImageUri!!).exists()) {
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


            } else {
                Toast.makeText(this, "Please correct the highlighted errors", Toast.LENGTH_SHORT).show()
            }

        }
//        submitButton.setOnClickListener {
//            submitPersonalDetails()
////            startActivity(Intent(this@PersonalInformation, PersonalDocuments::class.java))
////            finish()
//        }

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
                    // Format the selected date as yyyy-MM-dd
                    val date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
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
    fun validateFields(): Boolean {
        var isValid = true

        // List of all fields to validate
        val fields = listOf(
            Pair(findViewById<EditText>(R.id.firstNameInput), "First Name"),
            Pair(findViewById<EditText>(R.id.lastNameInput), "Last Name"),
            Pair(findViewById<EditText>(R.id.fatherNameInput), "Father Name"),
            Pair(findViewById<EditText>(R.id.dateOfBirthInput), "Date of Birth"),
            Pair(findViewById<EditText>(R.id.bloodGroupInput), "Blood Group"),
            Pair(findViewById<EditText>(R.id.cityInput1), "City"),
            Pair(findViewById<EditText>(R.id.languagesInput), "Languages Known")
        )

        // Validate all fields for empty input
        for ((field, fieldName) in fields) {
            if (field.text.toString().trim().isEmpty()) {
                // Mark the field with a red border
                field.setBackgroundResource(R.drawable.red_border)
//                field.error = "$fieldName is required"
                isValid = false
            } else {
                // Reset to default background
                field.setBackgroundResource(R.drawable.default_border)
            }
        }

        // Validate phone numbers
        val primaryPhone = findViewById<EditText>(R.id.primaryPhoneInput)
        val secondaryPhone = findViewById<EditText>(R.id.secondaryPhoneInput)

        if (primaryPhone.text.toString().trim().length != 10) {
            primaryPhone.setBackgroundResource(R.drawable.red_border)
            primaryPhone.error = "Primary Phone must be 10 digits"
            isValid = false
        } else {
            primaryPhone.setBackgroundResource(R.drawable.default_border)
        }

        if (secondaryPhone.text.toString().trim().isNotEmpty() && secondaryPhone.text.toString().trim().length != 10) {
            secondaryPhone.setBackgroundResource(R.drawable.red_border)
            secondaryPhone.error = "Secondary Phone must be 10 digits if provided"
            isValid = false
        } else {
            secondaryPhone.setBackgroundResource(R.drawable.default_border)
        }

        return isValid
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
        //image part
        val imageFile = uriToFile(profileImageUri!!)
        val imagePart = MultipartBody.Part.createFormData(
            "img", // Field name for the image
            imageFile.name, // File name
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile) // RequestBody for the image file
        )

//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("firstName", "sud")
//            .addFormDataPart("lastName", "han")
//            .addFormDataPart("fatherName", "va")
//            .addFormDataPart("dob","1999-12-12" ) // Example date of birth
//            .addFormDataPart("phone", "9113854167")
//            .addFormDataPart("secondaryNumber", "91111111111")
//            .addFormDataPart("bloodGroup", "o+")
//            .addFormDataPart("city", "Bangalore")
//            .addFormDataPart("address", addressJson.toString()) // Send address as a JSON string
//            .addFormDataPart("languageKnown", "languageKnown")
//            .addPart(imagePart) // Attach the image part here
//            .build()
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
                Log.e("DEBUG", "Request failed: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DEBUG", "Response received")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("DEBUG", "Response Body: $responseBody")

                    runOnUiThread {
                        Log.d("DEBUG", "Navigating to PersonalDocuments")
                        startActivity(Intent(this@PersonalInformation, PersonalDocuments::class.java))
                        finish()
                    }
                } else {
                    val errorBody = response.body?.string() ?: "No error body"
                    Log.e("DEBUG", "API Failure: Code: ${response.code}, Error Body: $errorBody")
                }
            }
        })
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

