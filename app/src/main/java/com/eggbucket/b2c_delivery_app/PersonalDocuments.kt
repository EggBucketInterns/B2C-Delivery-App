package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PersonalDocuments : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_documents)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.personalDocuments)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Redirects to Personal Information
        val personalInfoButton: LinearLayout= findViewById(R.id.linearPersonalInformation)
        personalInfoButton.setOnClickListener {
            val intent = Intent(this, PersonalInformation::class.java)
            startActivity(intent)
        }
        //Redirects to Bank Account Details
        val bankDocumentButton: LinearLayout= findViewById(R.id.linearBankDocuments)
        bankDocumentButton.setOnClickListener {
            val intent = Intent(this, BankAccountDetails::class.java)
            startActivity(intent)
        }
        //Redirects to Vehicle Details
        val vehicleButton: LinearLayout= findViewById(R.id.linearVehicleDocuments)
        vehicleButton.setOnClickListener {
            val intent = Intent(this, VehicleDetails::class.java)
            startActivity(intent)
        }
        //Redirects to Documents
        val personalDocButton: LinearLayout= findViewById(R.id.linearPersonalDocuments)
        personalDocButton.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivity(intent)
        }
        val submitButton: TextView= findViewById(R.id.submitButtonDocs)
        submitButton.setOnClickListener {
            val intent = Intent(this, SubmittedApplication::class.java)
            startActivity(intent)
        }

    }
}