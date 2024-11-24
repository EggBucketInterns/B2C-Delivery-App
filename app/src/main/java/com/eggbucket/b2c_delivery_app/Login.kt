package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val button: Button = findViewById(R.id.btnLogin)
        button.setOnClickListener {
            // Create an Intent to navigate to the second activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        val signupbtn: TextView = findViewById(R.id.register_btn)
        signupbtn.setOnClickListener {
            // Create an Intent to navigate to the second activity
            val intent = Intent(this, PersonalInformation::class.java)
            startActivity(intent)
        }
    }
}