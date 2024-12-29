package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.SharedPreferences
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class Delivery : Fragment() {
    private lateinit var usersharedPreferences: SharedPreferences
    private lateinit var ordersharedPreferences: SharedPreferences
    private lateinit var orderNumber:String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delivery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnApplyWindowInsetsListener { v, insets ->
            val statusBarHeight = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                insets.getInsets(android.view.WindowInsets.Type.statusBars()).top
            } else {
                insets.systemWindowInsetTop
            }
            v.setPadding(0, statusBarHeight, 0, 0) // Apply padding to the top
            insets
        }
        usersharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val riderPhoneNo=usersharedPreferences.getString("phone", "")
        ordersharedPreferences = requireContext().getSharedPreferences("OrderPrefs", Context.MODE_PRIVATE)
        val stringJson = ordersharedPreferences.getString("SelectedOrderData", null)


        if (stringJson.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Order data not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val jsonData = JSONObject(stringJson)

            // Retrieve values
            orderNumber = jsonData.optString("orderNumber", "N/A")
            val orderValue = jsonData.optString("orderValue", "N/A")


            // Delivery address
            val deliveryAddressJson = jsonData.optJSONObject("deliveryAddress")?.optJSONObject("fullAddress")
            val deliveryAddress = deliveryAddressJson?.let {
                "${it.optString("flatNo", "")} ${it.optString("area", "")}, ${it.optString("city", "")}, ${it.optString("state", "")} - ${it.optString("zipCode", "")}, ${it.optString("country", "")}"
            } ?: "Delivery address not available"
            val customerName = jsonData.optJSONObject("customerInfo").optString("name", "N/A")

            val products: JSONObject = jsonData.optJSONObject("products")
            val e6Quantity = products.optInt("E6", 0)
            val e12Quantity = products.optInt("E12", 0)
            val e30Quantity = products.optInt("E30", 0)


            view.findViewById<TextView>(R.id.orderNumber).text = "Order No: $orderNumber"
            view.findViewById<TextView>(R.id.orderValue).text = "Order Value: ₹$orderValue"
            view.findViewById<TextView>(R.id.deliveryAddress).text = deliveryAddress
            view.findViewById<TextView>(R.id.coustmerName)?.text = customerName
            view.findViewById<TextView>(R.id.eggs_6)?.text = e6Quantity.toString()
            view.findViewById<TextView>(R.id.eggs_12)?.text = e12Quantity.toString()
            view.findViewById<TextView>(R.id.eggs_30)?.text = e30Quantity.toString()

            // Handle back button click

            view.findViewById<ImageView>(R.id.backIcon).setOnClickListener {
                activity?.onBackPressed()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to parse order data", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<ImageView>(R.id.CallCoust).setOnClickListener(){

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+919113854167")
            }
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.confirmDelivery).setOnClickListener(){
            markAsDelivered(orderNumber,riderPhoneNo)


        }
    }
    private fun markAsDelivered(orderNumber:String,riderPhoneNo:String?){



        val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Set connect timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
                .build()
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/markorderdelivered/$riderPhoneNo/$orderNumber"

            // Create the PATCH request
        val request = Request.Builder()
            .url(url)
            .patch(RequestBody.create(null, ByteArray(0))) // Empty body for PATCH
            .build()

        // Execute the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network or other errors
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to mark order as delivered: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        // Navigate on the main thread
                        requireActivity().runOnUiThread {
                            findNavController().navigate(R.id.action_delivery_to_dashboardFragment)
                            Toast.makeText(requireContext(), "Order marked as delivered successfully.", Toast.LENGTH_SHORT).show()
                            val editor = ordersharedPreferences.edit()
                            editor.clear()
                            editor.apply()
                        }
                    } else {
                        // Handle server errors on the main thread
                        findNavController().navigate(R.id.action_delivery_to_dashboardFragment)
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Failed marking delivery: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
