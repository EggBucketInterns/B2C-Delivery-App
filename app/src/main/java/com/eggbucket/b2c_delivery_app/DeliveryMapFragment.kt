package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.widget.Button
import android.widget.ImageButton

class DeliveryMapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivery_map, container, false)

        val viewOrderDetailsButton: Button = view.findViewById(R.id.view_order_details)
        val googleMapsBtn: ImageButton =view.findViewById(R.id.google_maps_btn)

        viewOrderDetailsButton.setOnClickListener {
            val navController: NavController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_deliveryMapFragment2_to_orderDetails)
        }
        googleMapsBtn.setOnClickListener{
            val latitude = 12.9494
            val longitude = 77.5847

            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                )
                startActivity(browserIntent)
            }
        }

        return view
    }
}
