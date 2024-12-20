package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class OrderDetails : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false)
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

            // Outlet info
            val outletInfo = jsonData.optJSONObject("outletInfo")
            val outletName = outletInfo?.optString("name", "Unknown Outlet") ?: "Unknown Outlet"
            val outletAddressJson = outletInfo?.optJSONObject("address")?.optJSONObject("fullAddress")
            val outletAddress = outletAddressJson?.let {
                "${it.optString("flatNo", "")} ${it.optString("area", "")}, ${it.optString("city", "")}, ${it.optString("state", "")} - ${it.optString("zipCode", "")}, ${it.optString("country", "")}"
            } ?: "Address not available"

            // Delivery address
            val deliveryAddressJson = jsonData.optJSONObject("deliveryAddress")?.optJSONObject("fullAddress")
            val deliveryAddress = deliveryAddressJson?.let {
                "${it.optString("flatNo", "")} ${it.optString("area", "")}, ${it.optString("city", "")}, ${it.optString("state", "")} - ${it.optString("zipCode", "")}, ${it.optString("country", "")}"
            } ?: "Delivery address not available"
            val customerName = jsonData.optJSONObject("customerInfo").optString("name", "N/A")

            // Update UI
            view.findViewById<TextView>(R.id.outletName).text = outletName
            view.findViewById<TextView>(R.id.orderNumber).text = "Order No: $orderNumber"
            view.findViewById<TextView>(R.id.orderValue).text = "Order Value: ₹$orderValue"
            view.findViewById<TextView>(R.id.outletAddress).text = outletAddress
            view.findViewById<TextView>(R.id.deliveryAddress).text = deliveryAddress
            view?.findViewById<TextView>(R.id.coustmerName)?.text = customerName

            // Handle back button click
            view.findViewById<ImageView>(R.id.backIcon).setOnClickListener {
                activity?.onBackPressed()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to parse order data", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.conformPickup).setOnClickListener(){
            findNavController().navigate(R.id.action_orderDetails_to_deliveryMapFragment2)
        }

    }


}
