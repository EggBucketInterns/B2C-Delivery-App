package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.launch
import org.json.JSONException
import java.util.Calendar
import kotlin.math.pow

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var amount: TextView
    private lateinit var earningsText: TextView
    private lateinit var completedOrdersCountTextView: TextView
    private lateinit var ongoingOrderCount: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val phone = sharedPreferences.getString("phone_no", "null") ?: "null"

        // Initialize monthly earnings data
        initializeMonthlyData()

        // Update today's earnings for demonstration purposes
        updateEarningsForToday(1000)

        // Plot earnings graph
        plotEarningsGraph(view)

        // Update UI elements
        val earnings = calculateTotalEarnings()
        earningsText = view.findViewById(R.id.total_earnings)
        earningsText.text = "₹$earnings"

        amount = view.findViewById(R.id.amount)
        completedOrdersCountTextView = view.findViewById(R.id.completed_orders_count)
        ongoingOrderCount = view.findViewById(R.id.order_count)

        // Set up click listeners
        val orderRequestLayout = view.findViewById<LinearLayout>(R.id.Order_request)
        orderRequestLayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_deliveredOrders)
        }

        val orderCompleted = view.findViewById<LinearLayout>(R.id.total_orders_completed_layout)
        orderCompleted.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_orderSummary)
        }

        // Fetch data from APIs
        fetchOrdersCount(phone)
        fetchOngoingOrdersCount(phone)
        fetchFromApiAndStore(phone)
        fetchamount(phone)
    }

    /**
     * Initializes earnings data for the current month.
     * Resets data if it's a new month.
     */
    private fun initializeMonthlyData() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val storedMonth = sharedPreferences.getInt("month", -1)
        val storedYear = sharedPreferences.getInt("year", -1)

        if (storedMonth != currentMonth || storedYear != currentYear) {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putInt("month", currentMonth)
            editor.putInt("year", currentYear)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (day in 1..daysInMonth) {
                editor.putInt("day_$day", 0)
            }
            editor.apply()
        }
    }

    /**
     * Updates earnings for the current day.
     */
    private fun updateEarningsForToday(newEarnings: Int) {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentEarnings = sharedPreferences.getInt("day_$dayOfMonth", 0)
        sharedPreferences.edit().putInt("day_$dayOfMonth", currentEarnings + newEarnings).apply()
    }

    /**
     * Calculates total earnings for the current month.
     */
    private fun calculateTotalEarnings(): Int {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var totalEarnings = 0
        for (day in 1..daysInMonth) {
            totalEarnings += sharedPreferences.getInt("day_$day", 0)
        }
        return totalEarnings
    }

    /**
     * Fetches earnings data and plots the graph.
     */
    private fun plotEarningsGraph(view: View) {
        val graphView: GraphView = view.findViewById(R.id.graph)

        // Generate data points from SharedPreferences
        val dataPoints = mutableListOf<DataPoint>()
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var maxEarnings = 0.0 // To dynamically adjust the Y-axis

        for (day in 1..daysInMonth) {
            val earnings = sharedPreferences.getInt("day_$day", 0).toDouble()
            dataPoints.add(DataPoint(day.toDouble(), earnings))
            if (earnings > maxEarnings) {
                maxEarnings = earnings
            }
        }

        val series = LineGraphSeries<DataPoint>(dataPoints.toTypedArray())
        series.color = resources.getColor(R.color.orange)
        graphView.addSeries(series)

        // Configure graph appearance
        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(1.0)
        graphView.viewport.setMaxX(daysInMonth.toDouble())

        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(maxEarnings + (maxEarnings * 0.1)) // Add 10% padding for better visualization

        graphView.viewport.isScalable = true // Allow zooming
        graphView.viewport.isScrollable = true // Allow scrolling

        // Set graph height dynamically (optional, programmatically adjust height)
        val layoutParams = graphView.layoutParams
        layoutParams.height = 500 // Adjust the height as needed (in pixels)
        graphView.layoutParams = layoutParams
    }



    private fun fetchamount(phone:String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/storeamount/${phone}"

        val queue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                Log.d("value",response.toString())
                val value = response.getInt("amount")
                Log.d("valur",value.toString())
                // Update the completed orders count TextView with the fetched data
                amount.text = value.toString()

                // Once data is fetched, hide the loading drawable and show the actual data
                amount.setBackgroundResource(0) // Remove the loading drawable
            },
            { error ->
                Log.d("value",error.toString())
                // Handle error (e.g., display a message or a default value)
                amount.text = "0"

                // Remove the loading drawable in case of error as well
                amount.setBackgroundResource(0)
            })

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest)
    }

    private fun fetchOrdersCount(phone:String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/${phone}"

        val queue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val totalOrders = response.getInt("totalOrders")

                // Update the completed orders count TextView with the fetched data
                completedOrdersCountTextView.text = totalOrders.toString()

                // Once data is fetched, hide the loading drawable and show the actual data
                completedOrdersCountTextView.setBackgroundResource(0) // Remove the loading drawable

            },
            { error ->
                // Handle error (e.g., display a message or a default value)
                completedOrdersCountTextView.text = "0"

                // Remove the loading drawable in case of error as well
                completedOrdersCountTextView.setBackgroundResource(0)

            })

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest)
    }

    private fun fetchOngoingOrdersCount(phone:String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/${phone}"

        val queue = Volley.newRequestQueue(requireContext())

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    // Log the response for debugging
                    Log.d("FetchOrders", "Response: $response")

                    // Count the number of orders in the response
                    val count = response.length()

                    // Log the order count
                    Log.d("FetchOrders", "Order count: $count")

                    // Update the ongoing orders count TextView
                    ongoingOrderCount.text = count.toString()

                    // Once data is fetched, hide the loading drawable and show the actual data
                    ongoingOrderCount.setBackgroundResource(0) // Remove the loading drawable
                } catch (e: JSONException) {
                    Log.e("FetchOrders", "JSON Parsing Error: ${e.message}", e)
                    // Handle JSON parsing error
                    ongoingOrderCount.text = "0"
                    ongoingOrderCount.setBackgroundResource(0)
                }
            },
            { error ->
                // Log the error response for debugging
                Log.e("FetchOrders", "Volley Error: ${error.message}", error)

                // Handle error (e.g., display a message or a default value)
                ongoingOrderCount.text = "0"

                // Remove the loading drawable in case of error as well
                ongoingOrderCount.setBackgroundResource(0)
            })

        // Add the request to the RequestQueue
        queue.add(jsonArrayRequest)

        // Log that the request has been added to the queue
        Log.d("FetchOrders", "Request added to the queue")
    }




    private fun fetchFromApiAndStore(phone:String) {

        Log.d("general details", "API call")

        lifecycleScope.launch {
            try {
                // Call the API using suspend function
                val apiResponse = RetrofitClient.apiService.getGeneralDetails(phone)

                // Extract general details
                val generalDetails = apiResponse.generalDetails
                Log.d("general details", "General details: $generalDetails")

                // Convert Timestamp to string (example format: YYYY-MM-DD)
                val dob = generalDetails.dob.let {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(it._seconds * 1000))
                }.orEmpty()
                val addr=generalDetails.address.fullAddress
                val address="${addr.addressLine1}+${addr.addressLine2}+${addr.area}+${addr.city}+${addr.state}+${addr.zip}"

//                 Save to SharedPreferences
                saveToPreferences(
                    generalDetails.firstName,
                    generalDetails.lastName,
                    generalDetails.fatherName,
                    dob,
                    generalDetails.phone,
                    generalDetails.secondaryNumber,
                    generalDetails.bloodGroup,
                    generalDetails.city,
                    address,
                    generalDetails.languageKnown.joinToString(", "),
                    generalDetails.image
                )
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error occurred: ${e.message}", e)
            }
        }
    }

    private fun saveToPreferences(
        firstName: String, lastName: String, fatherName: String, dob: String,
        phone: String, secondaryNumber: String, bloodGroup: String, city: String,
        address: String, languageKnown: String, img: String
    ) {
        sharedPreferences.edit().apply {
            putBoolean("isDataStored", true)
            putString("firstName", firstName)
            putString("lastName", lastName)
            putString("fatherName", fatherName)
            putString("dob", dob)
            putString("phone", phone)
            putString("secondaryNumber", secondaryNumber)
            putString("bloodGroup", bloodGroup)
            putString("city", city)
            putString("address", address)
            putString("languageKnown", languageKnown)
            putString("img", img)
            apply()
        }
    }
}
