package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class Login : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Retrieve Firebase Device Token
        fetchDeviceToken(sharedPreferences)

        // Request Notification Permission for Android 13+
        requestNotificationPermission()

        val signupBtn: TextView = findViewById(R.id.register_btn)
        setupSignUpButton(sharedPreferences, signupBtn)

        val loginButton: Button = findViewById(R.id.btnLogin)
        val userIdEditText: EditText = findViewById(R.id.editTextUserId)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)

        loginButton.setOnClickListener {
            handleLogin(sharedPreferences, userIdEditText, passwordEditText)
        }
    }

    private fun fetchDeviceToken(sharedPreferences: SharedPreferences) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val deviceToken = task.result ?: ""
                if (deviceToken.isNotEmpty()) {
                    Log.d("FCM", "Device Token: $deviceToken")
                    sharedPreferences.edit().putString("device_token", deviceToken).apply()
                } else {
                    Log.e("FCM", "Device Token is empty")
                }
            } else {
                Log.e("FCM", "Failed to retrieve device token: ${task.exception?.message}")
            }
        }

    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),
                    1001
                )
            }
        }
    }

    private fun setupSignUpButton(sharedPreferences: SharedPreferences, signupBtn: TextView) {
        val status = sharedPreferences.getString("status", "default")
        if (status == "submitted-all") {
            signupBtn.text = "View Document Status"
            signupBtn.setOnClickListener {
                startActivity(Intent(this, DocumentStatus::class.java))
            }
        } else {
            signupBtn.setOnClickListener {
                startActivity(Intent(this, PersonalInformation::class.java))
            }
        }
    }

    private fun handleLogin(
        sharedPreferences: SharedPreferences,
        userIdEditText: EditText,
        passwordEditText: EditText
    ) {
        val phone = userIdEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val token = sharedPreferences.getString("device_token", "")

        // Validate the inputs
        if (token.isNullOrEmpty()) {
            Log.e("Login", "Token is empty or null")
            Toast.makeText(this, "Device token is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both phone and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the JSON body with the correct field names
        val jsonBody = JSONObject().apply {
            put("phone", phone)
            put("password", password)
            put("token", token)  // Update this field to "token"
        }

        sendPostRequest(jsonBody, phone)
    }


    private fun sendPostRequest(jsonBody: JSONObject, phoneNo: String) {
        // Extract the token from the JSON body using the new field name
        val token = jsonBody.optString("token", "")
        if (token.isEmpty()) {
            Log.e("Login", "Token is empty. Aborting request.")
            Toast.makeText(this, "Invalid token. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the URL as needed
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/verifypassword"

        // Media type for JSON request body
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody.toString())

        // Build the request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")  // Set the correct Content-Type
            .build()

        // Execute the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Failed to send request: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@Login, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("Login", "Response: $responseBody")
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_LONG).show()

                        // Store the login state in SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("status", "logged_in")
                            .putString("phone_no", phoneNo)
                            .apply()

                        // Navigate to the main activity after successful login
                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    } else {
                        Log.e("Login", "Error: HTTP ${response.code}: $responseBody")
                        Toast.makeText(this@Login, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }


}
