package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.json.JSONException
import org.json.JSONObject


class Delivery : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delivery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE)
        val stringJson = sharedPreferences.getString("SelectedOrderData", null)


        if (stringJson.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Order data not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val jsonData = JSONObject(stringJson)

            // Retrieve values
            val orderNumber = jsonData.optString("orderNumber", "N/A")
            val orderValue = jsonData.optString("orderValue", "N/A")
            // Delivery address
            val deliveryAddressJson = jsonData.optJSONObject("deliveryAddress")?.optJSONObject("fullAddress")
            val deliveryAddress = deliveryAddressJson?.let {
                "${it.optString("flatNo", "")} ${it.optString("area", "")}, ${it.optString("city", "")}, ${it.optString("state", "")} - ${it.optString("zipCode", "")}, ${it.optString("country", "")}"
            } ?: "Delivery address not available"
            val customerName = jsonData.optJSONObject("customerInfo").optString("name", "N/A")
            // Update UI

            view.findViewById<TextView>(R.id.orderNumber).text = "Order No: $orderNumber"
            view.findViewById<TextView>(R.id.orderValue).text = "Order Value: ₹$orderValue"
            view.findViewById<TextView>(R.id.deliveryAddress).text = deliveryAddress
            view.findViewById<TextView>(R.id.coustmerName).text = customerName

            // Handle back button click


        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to parse order data", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.conformDelivery).setOnClickListener(){

            findNavController().navigate(R.id.action_delivery_to_dashboardFragment)
        }
    }
}
