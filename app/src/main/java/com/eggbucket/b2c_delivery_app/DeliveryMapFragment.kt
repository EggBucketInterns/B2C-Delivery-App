package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DeliveryMapFragment : Fragment() {
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var deliveryLatitude: Double? = null
    private var deliveryLongitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var jsonData: JSONObject
    private lateinit var map: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivery_map, container, false)

        val reachedBtn: TextView= view.findViewById(R.id.textView3)
        val addressTextView: TextView = view.findViewById(R.id.address_text)
        val name: TextView = view.findViewById(R.id.name)
        map=view.findViewById(R.id.map)
        val gps_crosshair: ImageButton = view.findViewById(R.id.gps_crosshair)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Retrieve JSON data from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE)
        val stringJson = sharedPreferences.getString("SelectedOrderData", null)

        if (stringJson != null) {
            jsonData = JSONObject(stringJson)
            val deliveryAddress = jsonData.getJSONObject("deliveryAddress")
            val fullAddress = deliveryAddress.getJSONObject("fullAddress")
            deliveryLatitude = deliveryAddress.getJSONObject("coordinates").getDouble("lat")
            deliveryLongitude = deliveryAddress.getJSONObject("coordinates").getDouble("long")

            val flatNo = fullAddress.getString("flatNo")
            val area = fullAddress.getString("area")
            val city = fullAddress.getString("city")
            val zipCode = fullAddress.getString("zipCode")

            // Concatenate address
            val address = "$flatNo, $area, $city, $zipCode"
            addressTextView.text = address
            val customerName = jsonData.optJSONObject("customerInfo").optString("name", "N/A")
            name.text=customerName

        } else {
            Toast.makeText(requireContext(), "Order data not found", Toast.LENGTH_SHORT).show()
        }

        // Fetch user's current location
        fetchCurrentLocation {
            updateDistanceAndButton(reachedBtn)
        }

        gps_crosshair.setOnClickListener(){
            userLatitude=deliveryLatitude
            userLongitude=deliveryLongitude
            updateDistanceAndButton(reachedBtn)
        }


        reachedBtn.setOnClickListener {
            if (isUserNearOutlet()) {
                Toast.makeText(requireContext(), "You have reached the destination", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_deliveryMapFragment_to_delivery)
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
        val startPoint = GeoPoint(deliveryLatitude!!,deliveryLongitude!!) // Replace with your fixed latitude & longitude
        mapController.setCenter(startPoint)

        val marker = Marker(map)
        marker.position = startPoint // Set the position of the marker
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Adjust the marker's anchor point
        marker.title = "Destination" // Set a title for the marker
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
        if (userLatitude == null || userLongitude == null || deliveryLatitude == null || deliveryLongitude == null) {
            return null
        }
        val results = FloatArray(1)
        Location.distanceBetween(userLatitude!!, userLongitude!!, deliveryLatitude!!, deliveryLongitude!!, results)
        return results[0].toDouble() // Distance in meters
    }

    private fun updateDistanceAndButton(reachedBtn: TextView) {
        val distance = calculateDistance()
        if (distance != null && distance < 15) {
            reachedBtn.text = "REACHED"
        } else {
            reachedBtn.text = "Go To Delivery"
        }
    }

    private fun isUserNearOutlet(): Boolean {
        val distance = calculateDistance()
        return distance != null && distance < 15
    }

    private fun openGoogleMaps() {
        if (deliveryLatitude != null && deliveryLongitude != null) {
            val gmmIntentUri = Uri.parse("google.navigation:q=$deliveryLatitude,$deliveryLongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
        }
    }

}