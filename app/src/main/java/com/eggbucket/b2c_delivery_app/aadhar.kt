package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.eggbucket.b2c_delivery_app.databinding.ActivityAadharBinding

class aadhar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aadhar)
        val linearlayout = findViewById<View>(R.id.linear_layout_aadhar)
        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val prevButtonDoc: Button = findViewById(R.id.aadharBackButton)
        prevButtonDoc.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivity(intent)
        }
        val submitbutton: TextView = findViewById(R.id.submit_aadhar_btn)
        submitbutton.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivity(intent)
        }

    }
}