package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val db = Firebase.firestore
    private val partnerId = "0987654321"  // TODO: Replace with actual dynamic ID

    private lateinit var profileImage: ImageView
    private lateinit var tvPartnerName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvPartnerId: TextView
    private lateinit var tvPhoneValue: TextView
    private lateinit var tvDlValue: TextView
    private lateinit var tvAadhaarValue: TextView
    private lateinit var btnEdit: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        tvPartnerName = view.findViewById(R.id.tvPartnerName)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        tvPhoneValue = view.findViewById(R.id.tvPhoneValue)
        tvDlValue = view.findViewById(R.id.tvDlValue)
        tvAadhaarValue = view.findViewById(R.id.tvAadhaarValue)
        tvPartnerId = view.findViewById(R.id.tvPartnerId)
        btnEdit = view.findViewById(R.id.btnEditProfile)

        tvPartnerId.text = "ID: $partnerId"

        // ✅ Navigation to Edit Profile
        btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // ✅ Back button setup
        val backButton = view.findViewById<View>(R.id.profileBackButton)
        backButton?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        db.collection("Delivery_partner").document(partnerId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val general = doc.get("generalDetails") as? Map<*, *>

                    val firstName = general?.get("firstName") as? String ?: ""
                    val lastName = general?.get("lastName") as? String ?: ""
                    val phone = general?.get("phone") as? String ?: doc.id
                    val dl = general?.get("dlNumber") as? String ?: ""
                    val aadhaar = general?.get("aadhaarNumber") as? String ?: ""
                    val role = "Delivery Partner"
                    val imageUrl = general?.get("image") as? String

                    tvPartnerName.text = "$firstName $lastName"
                    tvSubtitle.text = role
                    tvPhoneValue.text = phone
                    tvDlValue.text = dl
                    tvAadhaarValue.text = aadhaar

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(imageUrl).into(profileImage)
                    }
                } else {
                    Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
