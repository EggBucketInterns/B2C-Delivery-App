package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PersonalDocuments : AppCompatActivity() {

    private val REQUEST_CODE_VEHICLE_DETAILS = 2001
    private val REQUEST_CODE_DOCS = 2002
    private val REQUEST_CODE_BANK_DETAILS = 2003

    private lateinit var pendingDocsGroup: LinearLayout
    private lateinit var completedDocsGroup: LinearLayout
    private lateinit var vehicleButton: LinearLayout
    private lateinit var personalDocsButton: LinearLayout
    private lateinit var bankDetailsButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_documents)

        // Initialize groups and buttons
        pendingDocsGroup = findViewById(R.id.pendingDocumentsGroup)
        completedDocsGroup = findViewById(R.id.completedDocumentsGroup)

        vehicleButton = findViewById(R.id.linearVehicleDocuments)
        personalDocsButton = findViewById(R.id.linearPersonalDocuments)
        bankDetailsButton = findViewById(R.id.linearBankDocuments)

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
        // Remove vehicleButton from pendingDocsGroup and add it to completedDocsGroup
        if (vehicleButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(vehicleButton)
            completedDocsGroup.addView(vehicleButton)
        }
    }

    private fun moveDocsToCompleted() {
        // Remove personalDocsButton from pendingDocsGroup and add it to completedDocsGroup
        if (personalDocsButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(personalDocsButton)
            completedDocsGroup.addView(personalDocsButton)
        }
    }

    private fun moveBankDetailsToCompleted() {
        // Remove bankDetailsButton from pendingDocsGroup and add it to completedDocsGroup
        if (bankDetailsButton.parent == pendingDocsGroup) {
            pendingDocsGroup.removeView(bankDetailsButton)
            completedDocsGroup.addView(bankDetailsButton)
        }
    }
}
