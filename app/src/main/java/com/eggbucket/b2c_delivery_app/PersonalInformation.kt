package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PersonalInformation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_information)

        // Find the ScrollView by ID and set window insets
        val scrollView = findViewById<View>(R.id.scrollPersonal)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //On clicking submit, the app redirects the user to the Personal Documents page
        val submitButton: TextView = findViewById(R.id.submitButtonPersonalInfo)
        submitButton.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }
    }
}
