package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

import android.widget.TextView
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class PanCard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pan_card)
        val linearlayout = findViewById<View>(R.id.liniarlayoutpan)
        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val prevButtonDoc: Button = findViewById(R.id.panBackBtn)
        prevButtonDoc.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivity(intent)
        }
        val submitbutton: Button = findViewById(R.id.submit_pan_button)
        submitbutton.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivity(intent)
        }

    }
}