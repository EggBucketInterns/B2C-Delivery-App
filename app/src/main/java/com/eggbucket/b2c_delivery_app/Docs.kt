package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Docs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_docs)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.docs)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val prevButtonDoc: ImageView = findViewById(R.id.backArrowDocs)
        prevButtonDoc.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }
        val takeaadharbutton: TextView = findViewById(R.id.buttonTextAadharDocument)
        takeaadharbutton.setOnClickListener {
            val intent = Intent(this, Aadhar::class.java)
            startActivity(intent)
        }
        val takepanbutton: LinearLayout = findViewById(R.id.linearPANCard)
        takepanbutton.setOnClickListener {
            val intent = Intent(this, PanCard::class.java)
            startActivity(intent)
        }
        val takeDLbutton: LinearLayout = findViewById(R.id.linearDrivingLicense)
        takeDLbutton.setOnClickListener {
            val intent = Intent(this, DrivingLicense::class.java)
            startActivity(intent)
        }
        val submitbutton: TextView = findViewById(R.id.submitButtonPersonalDoc)
        submitbutton.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }

    }
}