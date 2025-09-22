package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class MainProfileFragment : Fragment() {

    private lateinit var personName: EditText
    private lateinit var backBtn: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "profiles"
    private val userId = "0987654321"  // make dynamic later

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_profile, container, false)

        personName = view.findViewById(R.id.personName)
        backBtn = view.findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        personName.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
        return view
    }

    private fun updateName() {
        val name = personName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf("name" to name)

        db.collection(collectionName)
            .document(userId)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Name updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
