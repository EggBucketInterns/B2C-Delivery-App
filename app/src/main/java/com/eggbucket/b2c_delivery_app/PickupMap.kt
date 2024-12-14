package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PickupMap : Fragment() {

    // Data classes for API response
    data class OutletResponse(
        val address: Address,
        val name: String,
        val distance: String,
        val img: String,
        val id: String
    )

    data class Address(
        val coordinates: Coordinates,
        val fullAddress: FullAddress
    )

    data class Coordinates(
        val lat: Double,
        val long: Double
    )

    data class FullAddress(
        val area: String,
        val country: String,
        val zipCode: String,
        val flatNo: String,
        val city: String,
        val state: String
    )

    // Variables for location and FusedLocationProviderClient
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var outletLatitude: Double? = null
    private var outletLongitude: Double? = null

    private lateinit var outletName: String
    private lateinit var outletAddress: FullAddress

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pickup_map, container, false)

        val reachedBtn: Button = view.findViewById(R.id.reached_outlet_button)
        val addressTextView: TextView = view.findViewById(R.id.address_text)
        val viewOrderDetailsButton: Button = view.findViewById(R.id.view_order_details)
        val googleMapsBtn: ImageButton = view.findViewById(R.id.google_maps_btn)
        val nameTextView:TextView=view.findViewById(R.id.name)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Fetch the user's current location
        fetchCurrentLocation()

        // Fetch outlet location from API
        fetchOutletLocation("7337786130") { fetchedAddress ->
            // Update the UI when data is fetched
            val addressText = "${fetchedAddress.flatNo}, ${fetchedAddress.area}, " +
                    "${fetchedAddress.city} - ${fetchedAddress.zipCode}, ${fetchedAddress.state}, ${fetchedAddress.country}"
            addressTextView.text = addressText
            nameTextView.text=outletName
        }

        // Button to navigate to order details
        viewOrderDetailsButton.setOnClickListener {
            findNavController().navigate(R.id.action_pickupMap_to_orderDetails)
        }

        // Button to open Google Maps navigation
        googleMapsBtn.setOnClickListener {
            if (outletLatitude != null && outletLongitude != null) {
                val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } else {
                Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
        val distance = calculateDistance(userLatitude, userLongitude, outletLatitude, outletLongitude)
        if (distance != null && distance < 15) {
            reachedBtn.text="REACHED OUTLET"
        }
        else{
            reachedBtn.text="GO TO OUTLET"
        }
        // Reached button logic
        reachedBtn.setOnClickListener {
            if (distance != null && distance < 15) {
                Toast.makeText(requireContext(), "You have reached the outlet", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pickupMap_to_orderDetails)
            } else {
                if (outletLatitude != null && outletLongitude != null) {
                    val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun fetchOutletLocation(phoneNumber: String, onFetchComplete: (FullAddress) -> Unit) {
        val apiService = RetrofitClient.apiService
        apiService.getOutletId(phoneNumber).enqueue(object : Callback<OutletResponse> {
            override fun onResponse(call: Call<OutletResponse>, response: Response<OutletResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { outletResponse ->
                        outletLatitude = outletResponse.address.coordinates.lat
                        outletLongitude = outletResponse.address.coordinates.long
                        outletAddress = outletResponse.address.fullAddress
                        outletName = outletResponse.name

                        // Notify fetch complete
                        onFetchComplete(outletAddress)

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

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLatitude = location.latitude
                userLongitude = location.longitude
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
            return null
        }
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble() // Distance in meters
    }
}
