package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView  // Import TextView correctly
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject

class DocumentStatus : AppCompatActivity() {  // Remove the generic part

    val API_URL: String = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getdocstatus/721"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.document_status)

        val linearlayout = findViewById<View>(R.id.LiniarLayoutDocstatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val fetchingStatusMessage = findViewById<TextView>(R.id.fetchingStatusMessage)
        val card1 = findViewById<CardView>(R.id.cardView1)
        val card2 = findViewById<CardView>(R.id.cardView2)
        val card3 = findViewById<CardView>(R.id.cardView3)
        val card4 = findViewById<CardView>(R.id.cardView4)

        // Show the loader and message
        progressBar.visibility = View.VISIBLE
        fetchingStatusMessage.visibility = View.VISIBLE
        card1.visibility = View.GONE
        card2.visibility = View.GONE
        card3.visibility = View.GONE
        card4.visibility = View.GONE



        ViewCompat.setOnApplyWindowInsetsListener(linearlayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            fetchDocumentStatus()  // Fetch the document status when the layout is ready.
            insets
        }
    }

    private fun fetchDocumentStatus() {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url(API_URL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("DocumentStatus", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("DocumentStatus", "Response: $responseData")

                    // Parse the JSON response
                    val jsonResponse = JSONObject(responseData)
                    val submissionStatus = jsonResponse.getJSONObject("submissionStatus")

                    // Get the status of each document
                    val personalDocsStatus = submissionStatus.getBoolean("personalDocs")
                    val vehicleDetailsStatus = submissionStatus.getBoolean("vehicleDetails")
                    val generalDetailsStatus = submissionStatus.getBoolean("generalDetails")
                    val bankDetailsStatus = submissionStatus.getBoolean("bankDetails")

                    // Log the status values
                    Log.d("DocumentStatus", "Personal Docs: $personalDocsStatus")
                    Log.d("DocumentStatus", "Vehicle Details: $vehicleDetailsStatus")
                    Log.d("DocumentStatus", "General Details: $generalDetailsStatus")
                    Log.d("DocumentStatus", "Bank Details: $bankDetailsStatus")

                    // Update the UI based on the response
                    runOnUiThread {
                        updateDocumentStatusUI(personalDocsStatus, vehicleDetailsStatus, generalDetailsStatus, bankDetailsStatus)
                        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                        val fetchingStatusMessage = findViewById<TextView>(R.id.fetchingStatusMessage)

                        progressBar.visibility = View.GONE
                        fetchingStatusMessage.visibility = View.GONE

                        findViewById<CardView>(R.id.cardView1).visibility = View.VISIBLE
                        findViewById<CardView>(R.id.cardView2).visibility = View.VISIBLE
                        findViewById<CardView>(R.id.cardView3).visibility = View.VISIBLE
                        findViewById<CardView>(R.id.cardView4).visibility = View.VISIBLE

                    }
                } else {
                    Log.e("DocumentStatus", "Request failed with code: ${response.code}")
                }
            }
        })
    }

    private fun updateDocumentStatusUI(personalDocs: Boolean, vehicleDetails: Boolean, generalDetails: Boolean, bankDetails: Boolean) {
        // Log the values that will be displayed in the UI
        Log.d("DocumentStatus", "Updating UI - Personal Docs: $personalDocs, Vehicle Details: $vehicleDetails, General Details: $generalDetails, Bank Details: $bankDetails")

        // Update Personal Information status
        val personalDocsTextView = findViewById<TextView>(R.id.textView3)
        val personalDocsButton = findViewById<ImageButton>(R.id.Upploadbutton)
        if (personalDocs) {
            personalDocsTextView.text = "Personal Information \n\n Approved"
            personalDocsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        } else {
            personalDocsTextView.text = "Personal Documents \n\n Verification Pending"
            personalDocsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        }

        // Update Vehicle Details status
        val vehicleDetailsTextView = findViewById<TextView>(R.id.textView5)
        val vehicleDetailsButton = findViewById<ImageButton>(R.id.Upploadbutton3)
        if (vehicleDetails) {
            vehicleDetailsTextView.text = "Vehicle Details \n\n Approved"
            vehicleDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        } else {
            vehicleDetailsTextView.text = "Vehicle Details \n\n Verification Pending"
            vehicleDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        }

        // Update General Details status
        val generalDetailsTextView = findViewById<TextView>(R.id.textView4)
        val generalDetailsButton = findViewById<ImageButton>(R.id.Upploadbutton1)
        if (generalDetails) {
            generalDetailsTextView.text = "General Details \n\n Approved"
            generalDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        } else {
            generalDetailsTextView.text = "General Details \n\n Verification Pending"
            generalDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        }

        // Update Bank Details status
        val bankDetailsTextView = findViewById<TextView>(R.id.textView6)
        val bankDetailsButton = findViewById<ImageButton>(R.id.Upploadbutton4)
        if (bankDetails) {
            bankDetailsTextView.text = "Bank Account Details \n\n Approved"
            bankDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        } else {
            bankDetailsTextView.text = "Bank Account Details \n\n Verification Pending"
            bankDetailsButton.setBackgroundResource(R.drawable.arrow)  // Change the icon if needed
        }
    }

}
