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

class SubmittedApplication  : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.submittedapplication)
        val linearlayout = findViewById<View>(R.id.liniarLayoutSuccess)
        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val DocStatusBtn: Button = findViewById(R.id.document_status)
        DocStatusBtn.setOnClickListener {
            val intent = Intent(this, DocumentStatus::class.java)
            startActivity(intent)
        }


    }
}