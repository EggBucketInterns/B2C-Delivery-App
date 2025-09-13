package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val db = Firebase.firestore
    // temporary hardcoded partnerId for testing. Replace with dynamic id (Auth or passed arg) later.
    private val partnerId = "0987654321"

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etDl: EditText
    private lateinit var etAadhaar: EditText
    private lateinit var btnSave: AppCompatButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bind views (must match your fragment_edit_profile.xml IDs)
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPhone = view.findViewById(R.id.etPhone)
        etEmail = view.findViewById(R.id.etEmail)
        etDl = view.findViewById(R.id.etDl)
        etAadhaar = view.findViewById(R.id.etAadhaar)
        btnSave = view.findViewById(R.id.btnSave)

        loadProfileData()

        btnSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun loadProfileData() {
        db.collection("Delivery_partner").document(partnerId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val general = doc.get("generalDetails") as? Map<*, *>

                    etFirstName.setText(general?.get("firstName") as? String ?: "")
                    etLastName.setText(general?.get("lastName") as? String ?: "")
                    // phone may be stored top-level or inside generalDetails — prefer general->phone then doc id
                    etPhone.setText(general?.get("phone") as? String ?: doc.getString("phone") ?: doc.id)
                    etEmail.setText(general?.get("email") as? String ?: doc.getString("email") ?: "")
                    etDl.setText(general?.get("dlNumber") as? String ?: "")
                    etAadhaar.setText(general?.get("aadhaarNumber") as? String ?: "")
                } else {
                    // No doc yet — keep fields empty (user can fill and save)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveProfileData() {
        // build the map exactly as ProfileFragment expects (generalDetails)
        val generalMap = hashMapOf(
            "firstName" to etFirstName.text.toString().trim(),
            "lastName" to etLastName.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "email" to etEmail.text.toString().trim(),
            "dlNumber" to etDl.text.toString().trim(),
            "aadhaarNumber" to etAadhaar.text.toString().trim()
        )

        val updateMap = hashMapOf<String, Any>(
            "generalDetails" to generalMap
        )

        // Use set(..., SetOptions.merge()) so the document is created if missing and merged if exists
        db.collection("Delivery_partner").document(partnerId)
            .set(updateMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                // return to ProfileFragment (which reloads onResume)
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
