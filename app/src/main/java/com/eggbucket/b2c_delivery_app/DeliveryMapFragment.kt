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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryMapFragment : Fragment() {
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private var outletLatitude: Double? = null
    private var outletLongitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var jsonData: JSONObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivery_map, container, false)

        val reachedBtn: TextView= view.findViewById(R.id.textView3)
        val addressTextView: TextView = view.findViewById(R.id.address_text)
        val name: TextView = view.findViewById(R.id.name)

        val gps_crosshair: ImageButton = view.findViewById(R.id.gps_crosshair)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Retrieve JSON data from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE)
        val stringJson = sharedPreferences.getString("SelectedOrderData", null)

        if (stringJson != null) {
            jsonData = JSONObject(stringJson)
            val deliveryAddress = jsonData.getJSONObject("deliveryAddress")
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
            userLatitude=outletLatitude
            userLongitude=outletLongitude
            updateDistanceAndButton(reachedBtn)
        }


        reachedBtn.setOnClickListener {
            if (isUserNearOutlet()) {
                Toast.makeText(requireContext(), "You have reached the outlet", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_deliveryMapFragment_to_delivery)
            } else {
                openGoogleMaps()
            }
        }

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