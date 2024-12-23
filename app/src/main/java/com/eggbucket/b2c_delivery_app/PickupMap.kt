package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Context.MODE_PRIVATE
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


import org.json.JSONObject
import org.osmdroid.config.Configuration

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class PickupMap : Fragment() {

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var outletLatitude: Double? = null
    private var outletLongitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var jsonData: JSONObject
    private lateinit var map: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pickup_map, container, false)

        val reachedBtn: Button = view.findViewById(R.id.reached_outlet_button)
        val addressTextView: TextView = view.findViewById(R.id.address_text)
        val nameTextView: TextView = view.findViewById(R.id.name)
        map=view.findViewById(R.id.map)
        val gps_crosshair: ImageButton = view.findViewById(R.id.gps_crosshair)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Retrieve JSON data from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE)
        val stringJson = sharedPreferences.getString("SelectedOrderData", null)

        if (stringJson != null) {
           jsonData = JSONObject(stringJson)

            val editor = sharedPreferences.edit()
            editor.putString("SelectedOrderData", jsonData.toString())
            editor.apply()




            val deliveryAddress = jsonData.getJSONObject("outletInfo").getJSONObject("address")
            val fullAddress = deliveryAddress.getJSONObject("fullAddress")
            outletLatitude = deliveryAddress.getJSONObject("coordinates").getDouble("lat")
            outletLongitude = deliveryAddress.getJSONObject("coordinates").getDouble("long")

            val flatNo = fullAddress.getString("flatNo")
            val area = fullAddress.getString("area")
            val city = fullAddress.getString("city")
            val zipCode = fullAddress.getString("zipCode")

            // Concatenate address
            val address = "$flatNo, $area, $city, $zipCode"
            addressTextView.text = address

            // Extract outlet name
            val outletName = jsonData.getJSONObject("outletInfo").getString("name")
            nameTextView.text = outletName
        } else {
            Toast.makeText(requireContext(), "Order data not found", Toast.LENGTH_SHORT).show()
        }

        // Fetch user's current location
        fetchCurrentLocation {
            updateDistanceAndButton(reachedBtn)
        }
        gps_crosshair.setOnClickListener(){
            userLatitude=outletLatitude
            userLongitude=outletLongitude
            updateDistanceAndButton(reachedBtn)
        }


        reachedBtn.setOnClickListener {
            if (isUserNearOutlet()) {
                Toast.makeText(requireContext(), "You have reached the outlet", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pickupMap_to_orderDetails)
            } else {
                openGoogleMaps()
            }
        }

        //gps map
        val sharedPrefs = requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(requireContext(), sharedPrefs)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(18.0) // Adjust zoom level
        val startPoint = org.osmdroid.util.GeoPoint(
            outletLatitude!!,
            outletLongitude!!
        ) // Replace with your fixed latitude & longitude
        mapController.setCenter(startPoint)

        val marker = Marker(map)
        marker.position = startPoint // Set the position of the marker
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Adjust the marker's anchor point
        marker.title = "Outlet Location" // Set a title for the marker
        map.overlays.add(marker)


        return view
    }

    private fun fetchCurrentLocation(onLocationFetched: () -> Unit) {
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
                onLocationFetched()
            } else {
                Toast.makeText(requireContext(), "Unable to fetch current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(): Double? {
        if (userLatitude == null || userLongitude == null || outletLatitude == null || outletLongitude == null) {
            return null
        }
        val results = FloatArray(1)
        Location.distanceBetween(userLatitude!!, userLongitude!!, outletLatitude!!, outletLongitude!!, results)
        return results[0].toDouble() // Distance in meters
    }

    private fun updateDistanceAndButton(reachedBtn: Button) {
        val distance = calculateDistance()
        if (distance != null && distance < 15) {
            reachedBtn.text = "REACHED OUTLET"
        } else {
            reachedBtn.text = "GO TO OUTLET"
        }
    }

    private fun isUserNearOutlet(): Boolean {
        val distance = calculateDistance()
        return distance != null && distance < 15
    }

    private fun openGoogleMaps() {
        if (outletLatitude != null && outletLongitude != null) {
            val gmmIntentUri = Uri.parse("google.navigation:q=$outletLatitude,$outletLongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onResume() {
        super.onResume()
        map.onResume() // Required by OSMDroid
    }

    override fun onPause() {
        super.onPause()
        map.onPause() // Required by OSMDroid
    }
}

//    private fun fetchOutletLocation(phoneNumber: String, onFetchComplete: (FullAddress) -> Unit) {
//        val apiService = RetrofitClient.apiService
//        apiService.getOutletId(phoneNumber).enqueue(object : Callback<OutletResponse> {
//            override fun onResponse(call: Call<OutletResponse>, response: Response<OutletResponse>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { outletResponse ->
//                        outletLatitude = outletResponse.address.coordinates.lat
//                        outletLongitude = outletResponse.address.coordinates.long
//                        outletAddress = outletResponse.address.fullAddress
//                        outletName = outletResponse.name
//
//                        // Notify fetch complete
//                        onFetchComplete(outletAddress)
//
//                        Toast.makeText(
//                            requireContext(),
//                            "Outlet location fetched: $outletLatitude, $outletLongitude",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Failed to fetch outlet location", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<OutletResponse>, t: Throwable) {
//                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }


