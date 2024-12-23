package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_login)  // Set content view before accessing views
        //shared preference to check the status of app
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val status = sharedPreferences.getString("status", "default")


        if (status == "logedin") {
            // If the user is already logged in, navigate to MainActivity directly
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Prevent going back to login page
            return
        }
        //notification permission
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

        // Check the status and change the behavior of the signup button
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
        button.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get the FCM(Firebase Messaging Service) registration token
                val token = task.result
                Log.d("FCM", "Device Token:${token}")
                Toast.makeText(this, "token:${token}", Toast.LENGTH_SHORT).show()
                // Use the token (e.g., send it to your server)
            }
            // Save the status as "logedin" when the user logs in
            sharedPreferences.edit().putString("status", "logedin").apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Prevent going back to login page
        }
    }
}
