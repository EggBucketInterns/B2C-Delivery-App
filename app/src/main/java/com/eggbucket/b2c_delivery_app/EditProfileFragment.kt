package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val db = Firebase.firestore

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etDl: EditText
    private lateinit var etAadhaar: EditText

    // ✅ get current partnerId dynamically
    private val partnerId: String
        get() {
            val sharedPref = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            return sharedPref.getString("phone_no", "") ?: ""   // <-- same key as in Login.kt
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPhone = view.findViewById(R.id.etPhone)
        etEmail = view.findViewById(R.id.etEmail)
        etDl = view.findViewById(R.id.etDl)
        etAadhaar = view.findViewById(R.id.etAadhaar)

        val btnSave = view.findViewById<View>(R.id.btnSave)
        val backButton = view.findViewById<ImageView>(R.id.aadharBackButton)

        // Load existing details into inputs
        loadProfile()

        // ✅ Save button
        btnSave.setOnClickListener {
            saveProfile()
        }

        // ✅ Back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadProfile() {
        if (partnerId.isEmpty()) {
            Toast.makeText(requireContext(), "No Partner ID found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Delivery_partner").document(partnerId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val general = doc.get("generalDetails") as? Map<*, *>

                    etFirstName.setText(general?.get("firstName") as? String ?: "")
                    etLastName.setText(general?.get("lastName") as? String ?: "")
                    etPhone.setText(general?.get("phone") as? String ?: partnerId)
                    etEmail.setText(general?.get("email") as? String ?: "")
                    etDl.setText(general?.get("dlNumber") as? String ?: "")
                    etAadhaar.setText(general?.get("aadhaarNumber") as? String ?: "")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveProfile() {
        if (partnerId.isEmpty()) {
            Toast.makeText(requireContext(), "No Partner ID found", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "firstName" to etFirstName.text.toString().trim(),
            "lastName" to etLastName.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "email" to etEmail.text.toString().trim(),
            "dlNumber" to etDl.text.toString().trim(),
            "aadhaarNumber" to etAadhaar.text.toString().trim()
        )

        db.collection("Delivery_partner").document(partnerId)
            .update("generalDetails", updatedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // ✅ go back to Profile
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
