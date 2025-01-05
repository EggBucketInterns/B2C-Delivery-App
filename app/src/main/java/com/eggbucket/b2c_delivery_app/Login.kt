package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
        enableEdgeToEdge()  // Enable edge-to-edge after super.onCreate()

        setContentView(R.layout.activity_login)  // Set content view before accessing views

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val status = sharedPreferences.getString("status", "default")

        if (status == "logedin") {
            // If the user is already logged in, navigate to MainActivity directly
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Prevent going back to login page
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the POST_NOTIFICATIONS permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),
                    1001
                )
            }
        }

        val signupbtn: TextView = findViewById(R.id.register_btn)
        if (status == "submitted-all") {
            signupbtn.text = "View Document Status"
            signupbtn.setOnClickListener {
                val intent = Intent(this, DocumentStatus::class.java)
                startActivity(intent)
            }
        } else {
            signupbtn.setOnClickListener {
                val intent = Intent(this, PersonalInformation::class.java)
                startActivity(intent)
            }
        }

        val button: Button = findViewById(R.id.btnLogin)
        val editTextUserId: EditText = findViewById(R.id.editTextUserId)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)

        button.setOnClickListener {
            val phone = editTextUserId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both phone and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare JSON body
            val jsonBody = JSONObject()
            jsonBody.put("phone", phone)
            jsonBody.put("password", password)

            // Send POST request
            sendPostRequest(jsonBody)
        }
    }

    private fun sendPostRequest(jsonBody: JSONObject) {
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
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.d("Login", "Response: $responseBody")
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_LONG).show()

                        // Save the status as "logedin" when the user logs in
                        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                        sharedPreferences.edit().putString("status", "logedin").apply()

                        // Navigate to MainActivity
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                        finish()  // Prevent going back to login page
                    } else {
                        Log.e("Login", "Error: HTTP ${response.code}: $responseBody")
                        Toast.makeText(this@Login, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
