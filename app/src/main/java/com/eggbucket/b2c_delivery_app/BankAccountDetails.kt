package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BankAccountDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bank_account_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bankDetails)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prevButton: ImageView = findViewById(R.id.backArrowBank)
        prevButton.setOnClickListener {
            val intent = Intent(this, PersonalDocuments::class.java)
            startActivity(intent)
        }

        val submitBtn: TextView = findViewById(R.id.submitButtonBankDoc)
        submitBtn.setOnClickListener {
            // Simulate successful submission
            val resultIntent = Intent()
            resultIntent.putExtra("isBankDetailsSubmitted", true) // Send flag to PersonalDocuments
            setResult(RESULT_OK, resultIntent)
            finish() // Close this activity
        }
    }
}
