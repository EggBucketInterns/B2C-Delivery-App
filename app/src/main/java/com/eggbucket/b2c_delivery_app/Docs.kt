package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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
    }
}