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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        initializeMonthlyData()
        updateEarningsForToday(1000)
        plotEarningsGraph(view)

        earningsText = view.findViewById(R.id.total_earnings)
        earningsText.text = "₹${calculateTotalEarnings()}"

        amount = view.findViewById(R.id.amount)
        completedOrdersCountTextView = view.findViewById(R.id.completed_orders_count)
        ongoingOrderCount = view.findViewById(R.id.order_count)

        view.findViewById<LinearLayout>(R.id.Order_request).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_deliveredOrders)
        }
        view.findViewById<LinearLayout>(R.id.total_orders_completed_layout).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_orderSummary)
        }

        fetchOrdersCount(phone)
        fetchOngoingOrdersCount(phone)
        fetchFromApiAndStore(phone)
        fetchAmount(phone)
    }

    private fun initializeMonthlyData() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val storedMonth = sharedPreferences.getInt("month", -1)
        val storedYear = sharedPreferences.getInt("year", -1)

        if (storedMonth != currentMonth || storedYear != currentYear) {
            sharedPreferences.edit().clear().apply()
            sharedPreferences.edit().putInt("month", currentMonth).putInt("year", currentYear).apply()
        }
    }

    private fun updateEarningsForToday(newEarnings: Int) {
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentEarnings = sharedPreferences.getInt("day_$dayOfMonth", 0)
        sharedPreferences.edit().putInt("day_$dayOfMonth", currentEarnings + newEarnings).apply()
    }

    private fun calculateTotalEarnings(): Int {
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).sumOf { sharedPreferences.getInt("day_$it", 0) }
    }

    private fun plotEarningsGraph(view: View) {
        val graphView: GraphView = view.findViewById(R.id.graph)
        val dataPoints = mutableListOf<DataPoint>()
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        var maxEarnings = 0.0

        for (day in 1..daysInMonth) {
            val earnings = sharedPreferences.getInt("day_$day", 0).toDouble()
            dataPoints.add(DataPoint(day.toDouble(), earnings))
            maxEarnings = maxOf(maxEarnings, earnings)
        }

        graphView.apply {
            addSeries(LineGraphSeries(dataPoints.toTypedArray()).apply { color = resources.getColor(R.color.orange) })
            viewport.apply {
                isXAxisBoundsManual = true
                setMinX(1.0)
                setMaxX(daysInMonth.toDouble())
                isYAxisBoundsManual = true
                setMinY(0.0)
                setMaxY(maxEarnings * 1.1)
                isScalable = true
                isScrollable = true
            }
        }
    }

    private fun fetchAmount(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/storeamount/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            try {
                amount.text = response.optInt("amount", 0).toString()
            } catch (e: JSONException) {
                Log.e("fetchAmount", "JSON Parsing Error: ${e.message}", e)
            }
        }, { error ->
            Log.e("fetchAmount", "Volley Error: ${error.message}", error)
            amount.text = "0"
        })
        queue.add(request)
    }

    private fun fetchOrdersCount(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            completedOrdersCountTextView.text = response.optInt("totalOrders", 0).toString()
        }, { error ->
            Log.e("fetchOrdersCount", "Volley Error: ${error.message}", error)
            completedOrdersCountTextView.text = "0"
        })
        queue.add(request)
    }

    private fun fetchOngoingOrdersCount(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            ongoingOrderCount.text = response.length().toString()
        }, { error ->
            Log.e("fetchOngoingOrders", "Volley Error: ${error.message}", error)
            ongoingOrderCount.text = "0"
        })
        queue.add(request)
    }

    private fun fetchFromApiAndStore(phone: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getGeneralDetails(phone)
                val details = response.generalDetails
                sharedPreferences.edit().apply {
                    putString("firstName", details.firstName)
                    putString("lastName", details.lastName)
                    putString("dob", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(details.dob._seconds * 1000))
                    putString("phone", details.phone)
                    apply()
                }
            } catch (e: Exception) {
                Log.e("fetchFromApi", "Error: ${e.message}", e)
            }
        }
    }
}
