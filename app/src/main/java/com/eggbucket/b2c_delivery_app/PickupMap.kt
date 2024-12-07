package com.eggbucket.b2c_delivery_app

import android.location.Location
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PickupMap : Fragment() {

    // Data classes for API response
    data class OutletResponse(
        val address: Address
    )

    data class Address(
        val coordinates: Coordinates
    )

    data class Coordinates(
        val lat: Double,
        val long: Double
    )

    // Variables for location and FusedLocationProviderClient
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var outletLatitude: Double? = null
    private var outletLongitude: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pickup_map, container, false)
        val reached_btn: Button=view.findViewById(R.id.reached_outlet_button)
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val viewOrderDetailsButton: Button = view.findViewById(R.id.view_order_details)
        val googleMapsBtn: ImageButton = view.findViewById(R.id.google_maps_btn)

        // Navigate to Order Details
        viewOrderDetailsButton.setOnClickListener {
            val navController: NavController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_pickupMap_to_orderDetails)
        }

        // Fetch the user's current location
        fetchCurrentLocation()

        // Fetch outlet location from API
        fetchOutletLocation("7337786130") // Replace with the actual phone number
        val dist=calculateDistance(userLatitude,userLongitude,outletLatitude,outletLongitude)
        if (dist == null) {
            reached_btn.text = "GO TO OUTLET"
            reached_btn.setOnClickListener {
                if (userLatitude != null && userLongitude != null && outletLatitude != null && outletLongitude != null) {
                    val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (dist < 15) {
            reached_btn.text = "OUTLET REACHED"
            reached_btn.setOnClickListener{
                findNavController().navigate(R.id.action_pickupMap_to_orderDetails)
            }
        } else {
            reached_btn.text = "GO TO OUTLET"
            reached_btn.setOnClickListener {
                if (userLatitude != null && userLongitude != null && outletLatitude != null && outletLongitude != null) {
                    val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                }
            }

        }


        // Launch Google Maps
        googleMapsBtn.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        return view
    }

    // Function to fetch outlet location from API
    private fun fetchOutletLocation(phoneNumber: String) {
        val apiService = RetrofitClient.apiService
        apiService.getOutletId(phoneNumber).enqueue(object : Callback<OutletResponse> {
            override fun onResponse(call: Call<OutletResponse>, response: Response<OutletResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { outletResponse ->
                        // Extract latitude and longitude from the response
                        outletLatitude = outletResponse.address.coordinates.lat
                        outletLongitude = outletResponse.address.coordinates.long

                        // Notify the user
                        Toast.makeText(
                            requireContext(),
                            "Outlet location fetched: $outletLatitude, $outletLongitude",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch outlet location", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OutletResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    // Function to fetch user's current location
    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Extract the latitude and longitude
                userLatitude = location.latitude
                userLongitude = location.longitude

                // Notify the user
                Toast.makeText(
                    requireContext(),
                    "User location: Lat: $userLatitude, Long: $userLongitude",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Unable to fetch current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double? {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null // Return null if any value is missing
        }
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        //returns distance in meters
        return results[0].toDouble()
    }
}