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

    private var isAadharSubmitted = false
    private var isPanCardSubmitted = false
    private var isDrivingLicenseSubmitted = false

    private lateinit var aadharStatusIcon: ImageView
    private lateinit var panCardStatusIcon: ImageView
    private lateinit var drivingLicenseStatusIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_docs)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.docs)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize status icons
        aadharStatusIcon = findViewById(R.id.buttonIconAadharDocument)
        panCardStatusIcon = findViewById(R.id.buttonIconPANCard)
        drivingLicenseStatusIcon = findViewById(R.id.buttonIconDrivingLicense)

        // Back Arrow
        findViewById<ImageView>(R.id.backArrowDocs).setOnClickListener {
            startActivity(Intent(this, PersonalDocuments::class.java))
        }

        // Aadhar Document
        findViewById<TextView>(R.id.buttonTextAadharDocument).setOnClickListener {
            startActivityForResult(Intent(this, Aadhar::class.java), REQUEST_AADHAR)
        }

        // PAN Card Document
        findViewById<LinearLayout>(R.id.linearPANCard).setOnClickListener {
            startActivityForResult(Intent(this, PanCard::class.java), REQUEST_PAN_CARD)
        }

        // Driving License Document
        findViewById<LinearLayout>(R.id.linearDrivingLicense).setOnClickListener {
            startActivityForResult(Intent(this, DrivingLicense::class.java), REQUEST_DRIVING_LICENSE)
        }

        // Submit Button
        val submitButton = findViewById<TextView>(R.id.submitButtonPersonalDoc)
        submitButton.setOnClickListener {
            if (isAadharSubmitted && isPanCardSubmitted && isDrivingLicenseSubmitted) {
                val resultIntent = Intent()
                resultIntent.putExtra("isDocsSubmitted", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                // Error feedback showing which documents are missing
                showMissingDocsMessage()
            }
        }

        // Initial validation to disable the submit button
        validateSubmitButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_AADHAR -> {
                    isAadharSubmitted = data?.getBooleanExtra("isAadharSubmitted", false) == true
                    if (isAadharSubmitted) updateStatusIcon(aadharStatusIcon, true)
                }
                REQUEST_PAN_CARD -> {
                    isPanCardSubmitted = data?.getBooleanExtra("isPanCardSubmitted", false) == true
                    if (isPanCardSubmitted) updateStatusIcon(panCardStatusIcon, true)
                }
                REQUEST_DRIVING_LICENSE -> {
                    isDrivingLicenseSubmitted = data?.getBooleanExtra("isDrivingLicenseSubmitted", false) == true
                    if (isDrivingLicenseSubmitted) updateStatusIcon(drivingLicenseStatusIcon, true)
                }
            }
            validateSubmitButton()
        }
    }

    private fun validateSubmitButton() {
        val submitButton = findViewById<TextView>(R.id.submitButtonPersonalDoc)
        submitButton.isEnabled = isAadharSubmitted || isPanCardSubmitted || isDrivingLicenseSubmitted
    }

    private fun showMissingDocsMessage() {
        val missingDocs = mutableListOf<String>()
        if (!isAadharSubmitted) missingDocs.add("Aadhar")
        if (!isPanCardSubmitted) missingDocs.add("PAN Card")
        if (!isDrivingLicenseSubmitted) missingDocs.add("Driving License")

        val message = if (missingDocs.isEmpty()) {
            "Please upload the required documents."
        } else {
            "Missing Documents: ${missingDocs.joinToString(", ")}"
        }

        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun updateStatusIcon(iconView: ImageView, isCompleted: Boolean) {
        val drawable = if (isCompleted) R.drawable.greentick else R.drawable.img_2
        iconView.setImageResource(drawable)
    }

    companion object {
        private const val REQUEST_AADHAR = 1
        private const val REQUEST_PAN_CARD = 2
        private const val REQUEST_DRIVING_LICENSE = 3
    }
}
