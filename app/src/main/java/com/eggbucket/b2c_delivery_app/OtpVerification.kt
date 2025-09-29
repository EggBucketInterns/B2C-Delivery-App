package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eggbucket.b2c_delivery_app.databinding.OtpVerifiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: OtpVerifiBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OtpVerifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId")
        phoneNumber = intent.getStringExtra("phoneNumber")

        binding.subtitleTextView.text = "Enter the code from the SMS we sent to\n+91 $phoneNumber"
        setupPinView()
        binding.submitButton.setOnClickListener {
            val otp = binding.otpPinView.text.toString()
            if (otp.length == 6 && verificationId != null) {
                verifyOtp(otp)
            } else {
                Toast.makeText(this, "Please enter the complete 6-digit OTP.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backImageView.setOnClickListener { finish() }
        startTimer()
    }

    private fun setupPinView() {
        binding.otpPinView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    binding.submitButton.isEnabled = false
                    verifyOtp(s.toString())
                }
            }
        })
    }


    private fun verifyOtp(otp: String) {
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful! Fetching details...", Toast.LENGTH_SHORT).show()
                    fetchAndSaveUserData()
                } else {
                    binding.submitButton.isEnabled = true
                    Log.w("OtpActivity", "signInWithCredential failed", task.exception)
                    Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchAndSaveUserData() {
        if (phoneNumber == null) {
            navigateToMainApp(isLoginComplete = false)
            return
        }

        val apiUrl = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/profile/$phoneNumber"
        val client = OkHttpClient()
        val request = Request.Builder().url(apiUrl).build()

        Log.d("API_CALL", "Attempting to fetch data from: $apiUrl")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_CALL_FAILURE", "Network request failed.", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Network Error: Could not fetch user details.", Toast.LENGTH_LONG).show()
                    navigateToMainApp(isLoginComplete = false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("API_RESPONSE", "Received response with code: ${response.code}")

                // --- MODIFICATION START ---
                // Check the HTTP status code to determine the user's status
                when (response.code) {
                    200 -> { // HTTP 200 OK: Existing user.
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                val generalDetailsJson = json.optJSONObject("generalDetails")

                                if (generalDetailsJson != null) {
                                    editor.putString("firstName", generalDetailsJson.optString("firstName", "N/A"))
                                    // ... add other fields from your API response
                                }

                                editor.putString("status", "logged_in")
                                editor.putString("phone_no", phoneNumber)
                                editor.apply()
                                Log.d("API_PARSE_SUCCESS", "Successfully parsed and saved existing user data.")
                                navigateToMainApp(isLoginComplete = true)
                            } catch (e: Exception) {
                                Log.e("API_PARSE_ERROR", "Error parsing JSON for existing user.", e)
                                navigateToMainApp(isLoginComplete = false)
                            }

                        } else {
                            navigateToMainApp(isLoginComplete = false)
                        }
                    }
                    404 -> { // HTTP 404 Not Found: New user.
                        Log.d("API_RESPONSE", "User not found (404). Navigating to registration.")
                        navigateToRegistration()
                    }
                    else -> { // Handle other server errors (500, 403, etc.)
                        Log.e("API_RESPONSE_ERROR", "API Error. Code: ${response.code}")
                        runOnUiThread {
                            Toast.makeText(applicationContext, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                response.body?.close() // Always close the response body to prevent resource leaks
                // --- MODIFICATION END ---
            }
        })
    }

    private fun navigateToMainApp(isLoginComplete: Boolean) {
        if (!isLoginComplete) {
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }

        runOnUiThread {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // --- NEW FUNCTION TO NAVIGATE TO THE REGISTRATION SCREEN ---
    private fun navigateToRegistration() {
        runOnUiThread {
//            // IMPORTANT: You must create a new Activity called 'RegistrationActivity' for this to work.
//            val intent = Intent(this, RegistrationActivity::class.java)
//            // Pass the phone number so the registration screen can use it
//            intent.putExtra("phoneNumber", phoneNumber)
//            // Clear the back stack
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
        }
    }
    // --- END OF NEW FUNCTION ---

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.timerTextView.text = String.format("00:%02d", seconds)
                binding.resendCodeTextView.isEnabled = false
            }

            override fun onFinish() {
                binding.timerTextView.text = "00:00"
                binding.resendCodeTextView.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}