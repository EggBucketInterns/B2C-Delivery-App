package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.eggbucket.b2c_delivery_app.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for buttons
            view.findViewById<View>(R.id.llPersonalInfo).setOnClickListener {
                // Start the Docs activity
                val intent = Intent(requireContext(), PersonalInformation::class.java)
                startActivity(intent)
            }

        view.findViewById<View>(R.id.llAddresses).setOnClickListener {
            // Handle Addresses button click
        }

        view.findViewById<View>(R.id.llOrderHistory).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_orderSummary)
        }

        view.findViewById<View>(R.id.llHelpSupport).setOnClickListener {
            // Handle Help and Support button click
        }

        view.findViewById<View>(R.id.llNotifications).setOnClickListener {
//            findNavController().navigate(R.id.action_profileFragment_to_newOrder)
        }

        view.findViewById<View>(R.id.llLogout).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_login)
        }
    }
}
