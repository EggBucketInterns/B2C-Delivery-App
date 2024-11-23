package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VehicleDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicle_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.vehicleDetails)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val prevButtonVehicle: ImageView = findViewById(R.id.backArrowVehicle)
        prevButtonVehicle.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }
        val submitButton: TextView = findViewById(R.id.submitButtonVehicleDoc)
        submitButton.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }

    }
}