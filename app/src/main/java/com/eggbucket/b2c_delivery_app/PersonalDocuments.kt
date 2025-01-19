package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PersonalDocuments : AppCompatActivity() {
    private val REQUEST_CODE_VEHICLE_DETAILS = 2001
    private val REQUEST_CODE_DOCS = 2002
    private val REQUEST_CODE_BANK_DETAILS = 2003
    private val REQUEST_CODE_PASSBOOK = 2004

    private lateinit var pendingDocsGroup: LinearLayout
    private lateinit var completedDocsGroup: LinearLayout
    private lateinit var vehicleButton: LinearLayout
    private lateinit var personalDocsButton: LinearLayout
    private lateinit var bankDetailsButton: LinearLayout
    private lateinit var passbookButton: LinearLayout
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_documents)

        // Initialize groups and buttons
        pendingDocsGroup = findViewById(R.id.pendingDocumentsGroup)
        completedDocsGroup = findViewById(R.id.completedDocumentsGroup)
        vehicleButton = findViewById(R.id.linearVehicleDocuments)
        personalDocsButton = findViewById(R.id.linearPersonalDocuments)
        bankDetailsButton = findViewById(R.id.linearBankDocuments)
        passbookButton = findViewById(R.id.linearPassbookDocuments)
        submitButton = findViewById(R.id.submitButtonDocs)

        // Handle Vehicle Details button click
        vehicleButton.setOnClickListener {
            val intent = Intent(this, VehicleDetails::class.java)
            startActivityForResult(intent, REQUEST_CODE_VEHICLE_DETAILS)
        }

        // Handle Personal Documents button click
        personalDocsButton.setOnClickListener {
            val intent = Intent(this, Docs::class.java)
            startActivityForResult(intent, REQUEST_CODE_DOCS)
        }

        // Handle Bank Account Details button click
        bankDetailsButton.setOnClickListener {
            val intent = Intent(this, BankAccountDetails::class.java)
            startActivityForResult(intent, REQUEST_CODE_BANK_DETAILS)
        }
        // Handle Passbook button click
        val passbookIcon: ImageView = findViewById(R.id.buttonIconPassbookDocuments)
        passbookIcon.setOnClickListener {
            val intent = Intent(this, Passbook::class.java)
            startActivityForResult(intent, REQUEST_CODE_PASSBOOK)
        }

        // Handle Submit button click
        submitButton.setOnClickListener {
            if (isSubmissionValid()) {
                Toast.makeText(this, "All documents submitted successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SubmittedApplication::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please complete all document submissions!", Toast.LENGTH_SHORT).show()
                Log.e("PersonalDocuments", "Submission validation failed: Not all documents are in the completed section.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_VEHICLE_DETAILS -> {
                if (resultCode == RESULT_OK) {
                    val isVehicleSubmitted = data?.getBooleanExtra("isVehicleDetailsSubmitted", false) ?: false
                    if (isVehicleSubmitted) {
                        moveVehicleDetailsToCompleted()
                    }
                }
            }
            REQUEST_CODE_DOCS -> {
                if (resultCode == RESULT_OK) {
                    val isDocsSubmitted = data?.getBooleanExtra("isDocsSubmitted", false) ?: false
                    if (isDocsSubmitted) {
                        moveDocsToCompleted()
                    }
                }
            }

            REQUEST_CODE_PASSBOOK -> {
                if (resultCode == RESULT_OK) {
                    val isPassbookSubmitted = data?.getBooleanExtra("isPassbookSubmitted", false) ?: false
                    if (isPassbookSubmitted) {
                        movePassbookToCompleted()
                    }
                }
            }
            REQUEST_CODE_BANK_DETAILS -> {
                if (resultCode == RESULT_OK) {
                    val isBankDetailsSubmitted = data?.getBooleanExtra("isBankDetailsSubmitted", false) ?: false
                    if (isBankDetailsSubmitted) {
                        moveBankDetailsToCompleted()
                    }
                }
            }
        }
    }

    private fun moveVehicleDetailsToCompleted() {
        if (vehicleButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(vehicleButton)
            completedDocsGroup.addView(vehicleButton)
        }
    }

    private fun moveDocsToCompleted() {
        if (personalDocsButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(personalDocsButton)
            completedDocsGroup.addView(personalDocsButton)
        }
    }

    private fun moveBankDetailsToCompleted() {
        if (bankDetailsButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(bankDetailsButton)
            completedDocsGroup.addView(bankDetailsButton)
        }
    }
    private fun movePassbookToCompleted() {
        if (passbookButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(passbookButton)
            completedDocsGroup.addView(passbookButton)
        }
    }

    private fun isSubmissionValid(): Boolean {
        return vehicleButton.parent == completedDocsGroup &&
                personalDocsButton.parent == completedDocsGroup &&
                bankDetailsButton.parent == completedDocsGroup
    }
}
