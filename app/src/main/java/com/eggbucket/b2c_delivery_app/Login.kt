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
            Log.e("Login", "Token is empty or null, re-fetching...")
            Toast.makeText(this, "Device token not ready, please wait and try again.", Toast.LENGTH_SHORT).show()
            // Optionally re-trigger token fetch
            fetchDeviceToken(sharedPreferences)
            return
        }

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both phone and password", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBody = JSONObject().apply {
            put("phone", phone)
            put("password", password)
            put("token", token)
        }

        sendPostRequest(jsonBody, phone)
    }


    private fun sendPostRequest(jsonBody: JSONObject, phoneNo: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/verifypassword"
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Failed to send request: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@Login, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Read response body once. Be careful to not call response.body?.string() multiple times.
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("Login", "Response successful: $responseBody")
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_LONG).show()

                        // Store the login state in SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("status", "logged_in")
                            .putString("phone_no", phoneNo) // Save the user's ID (phone number)
                            .apply()

                        // Navigate to the main activity after successful login
                        val intent = Intent(this@Login, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // --- IMPROVED ERROR HANDLING ---
                        // Parse specific error message from backend response body
                        var errorMessage = "Login failed: ${response.code}" // Fallback message
                        if (responseBody != null) {
                            try {
                                val jsonError = JSONObject(responseBody)
                                errorMessage = jsonError.optString("message", errorMessage)
                            } catch (e: Exception) {
                                Log.w("Login", "Could not parse error JSON body: $responseBody")
                            }
                        }
                        Log.e("Login", "Error: HTTP ${response.code}: $responseBody")
                        Toast.makeText(this@Login, errorMessage, Toast.LENGTH_LONG).show()
                        // --- END IMPROVEMENT ---
                    }
                }
            }
        })
    }
}