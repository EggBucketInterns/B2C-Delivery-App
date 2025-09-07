package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // --- UI Elements ---
    private lateinit var totalAmountTextView: TextView // Shows total cash collected (wallet amount)
    private lateinit var earningsTextView: TextView // Shows calculated commission/earnings
    private lateinit var completedOrdersCountTextView: TextView
    private lateinit var ongoingOrderCount: TextView
    private lateinit var graphView: GraphView

    // --- Dependencies ---
    private lateinit var sharedPreferences: SharedPreferences

    // --- Constants ---
    companion object {
        const val COMMISSION_RATE_PERCENTAGE = 5 // Example: 5% commission
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Initialization ---
        initializeViews(view)
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // --- Data Loading and Validation ---
        val phone = sharedPreferences.getString("phone_no", null)

        if (phone == null || phone == "null") {
            // CRITICAL FIX: Do not proceed if the user ID is invalid.
            Log.e("DashboardFragment", "User ID not found in SharedPreferences. Cannot fetch data.")
            Toast.makeText(context, "Session invalid. Please log in again.", Toast.LENGTH_LONG).show()
            setEmptyState() // Clear UI fields or show default message
        } else {
            // User ID is valid, proceed to fetch data from the server.
            loadDashboardData(phone)
        }

        // --- Graph Setup ---
        // The graph relies on local data, so it can be set up independently.
        initializeMonthlyDataForGraph()
        plotEarningsGraph()

        // --- Navigation ---
        setupNavigationListeners(view)
    }

    /**
     * Initializes all view components from the layout.
     */
    private fun initializeViews(view: View) {
        earningsTextView = view.findViewById(R.id.total_earnings)
        totalAmountTextView = view.findViewById(R.id.amount)
        completedOrdersCountTextView = view.findViewById(R.id.completed_orders_count)
        ongoingOrderCount = view.findViewById(R.id.order_count)
        graphView = view.findViewById(R.id.graph)
    }

    /**
     * Sets up click listeners for navigation.
     */
    private fun setupNavigationListeners(view: View) {
        view.findViewById<LinearLayout>(R.id.Order_request).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_deliveredOrders)
        }
        view.findViewById<LinearLayout>(R.id.total_orders_completed_layout).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_orderSummary)
        }
    }

    /**
     * Wrapper function to fetch all required data from the backend.
     * @param phone The validated delivery partner ID.
     */
    private fun loadDashboardData(phone: String) {
        fetchOrdersCount(phone)
        fetchOngoingOrdersCount(phone)
        fetchTotalAmountAndEarnings(phone)
        fetchProfileDetailsToCache(phone)
    }

    /**
     * Resets UI fields to default values, typically used when data loading fails or user is logged out.
     */
    private fun setEmptyState() {
        earningsTextView.text = "₹0"
        totalAmountTextView.text = "₹0"
        completedOrdersCountTextView.text = "0"
        ongoingOrderCount.text = "0"
    }

    // =====================================================================================
    // Earnings Calculation and Graphing Logic
    // =====================================================================================

    /**
     * Fetches the total cash collected (wallet amount) from the backend.
     * Calculates and displays both the cash amount and the derived commission earnings.
     * This function provides the primary source of truth for earnings.
     */
    private fun fetchTotalAmountAndEarnings(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/storeamount/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // 1. Get total cash collected from response (backend key is "amount")
                    val totalCashCollected = response.optInt("amount", 0)
                    totalAmountTextView.text = "₹$totalCashCollected"

                    // 2. Calculate earnings based on the total cash collected.
                    // Use a single, reliable source of truth for earnings calculation.
                    val totalEarnings = (totalCashCollected * COMMISSION_RATE_PERCENTAGE) / 100
                    earningsTextView.text = "₹$totalEarnings"

                } catch (e: JSONException) {
                    Log.e("fetchTotalAmount", "JSON Parsing Error: ${e.message}", e)
                }
            },
            { error ->
                Log.e("fetchTotalAmount", "Volley Error: ${error.message}", error)
                setEmptyState() // Reset values on error
            }
        )
        queue.add(request)
    }

    /**
     * Resets local daily earnings data if the month/year changes.
     * This ensures the graph always shows data for the current month only.
     */
    private fun initializeMonthlyDataForGraph() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val storedMonth = sharedPreferences.getInt("graph_month", -1)
        val storedYear = sharedPreferences.getInt("graph_year", -1)

        if (storedMonth != currentMonth || storedYear != currentYear) {
            // Clear only graph-related daily data by iterating through old keys if necessary,
            // or clear all if SharedPreferences is only used for this temporary data.
            // For simplicity here, we clear and reset a known range, but a prefix-based clear is safer.
            val editor = sharedPreferences.edit()
            for (i in 1..31) {
                editor.remove("day_$i")
            }
            editor.putInt("graph_month", currentMonth)
            editor.putInt("graph_year", currentYear)
            editor.apply()
        }
    }

    /**
     * Updates local daily earnings. Call this from where order delivery is confirmed.
     * IMPORTANT: This method must be called from another part of the app (e.g., after
     * markOrderDelivered API call succeeds) to populate the graph.
     *
     * @param orderCommission Commission earned from a single completed order.
     */
    private fun updateDailyEarnings(orderCommission: Int) {
        if (orderCommission <= 0) return // Don't process zero or negative commission updates.

        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentEarnings = sharedPreferences.getInt("day_$dayOfMonth", 0)
        sharedPreferences.edit()
            .putInt("day_$dayOfMonth", currentEarnings + orderCommission)
            .apply()

        // Refresh graph data in real-time if the user is on the dashboard when an order completes.
        if (isResumed) {
            plotEarningsGraph()
        }
    }

    /**
     * Plot a line graph where x-axis = day of month, y-axis = commission earned that day.
     * Data source is the locally stored SharedPreferences values.
     */
    private fun plotEarningsGraph() {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dataPoints = mutableListOf<DataPoint>()
        var maxEarnings = 0.0

        for (day in 1..daysInMonth) {
            val earnings = sharedPreferences.getInt("day_$day", 0).toDouble()
            dataPoints.add(DataPoint(day.toDouble(), earnings))
            maxEarnings = maxOf(maxEarnings, earnings)
        }

        // Ensure graph has a minimum height even if earnings are low or zero
        if (maxEarnings < 50.0) maxEarnings = 50.0

        graphView.apply {
            removeAllSeries()
            addSeries(LineGraphSeries(dataPoints.toTypedArray()).apply {
                color = resources.getColor(R.color.orange) // Ensure R.color.orange exists
                isDrawBackground = true
                thickness = 8
            })
            viewport.apply {
                isXAxisBoundsManual = true
                setMinX(1.0)
                setMaxX(daysInMonth.toDouble())
                isYAxisBoundsManual = true
                setMinY(0.0)
                setMaxY(maxEarnings * 1.1) // Add 10% padding to max Y value
                isScalable = true
                isScrollable = true
            }
            gridLabelRenderer.horizontalAxisTitle = "Day of Month"
            gridLabelRenderer.verticalAxisTitle = "Earnings (₹)"
        }
    }

    // =====================================================================================
    // Other Data Fetching Functions
    // =====================================================================================

    /**
     * Fetches the total count of completed orders.
     */
    private fun fetchOrdersCount(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                completedOrdersCountTextView.text = response.optInt("totalOrders", 0).toString()
            },
            { error ->
                Log.e("fetchOrdersCount", "Volley Error: ${error.message}", error)
                completedOrdersCountTextView.text = "0"
            }
        )
        queue.add(request)
    }

    /**
     * Fetches the count of currently ongoing/pending orders.
     */
    private fun fetchOngoingOrdersCount(phone: String) {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/$phone"
        val queue = Volley.newRequestQueue(requireContext())
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                ongoingOrderCount.text = response.length().toString()
            },
            { error ->
                Log.e("fetchOngoingOrders", "Volley Error: ${error.message}", error)
                ongoingOrderCount.text = "0"
            }
        )
        queue.add(request)
    }

    /**
     * Fetches detailed profile information and caches it in SharedPreferences for other fragments to use.
     */
    private fun fetchProfileDetailsToCache(phone: String) {
        lifecycleScope.launch {
            try {
                // Assuming RetrofitClient.apiService.getGeneralDetails(phone) exists
                val response = RetrofitClient.apiService.getGeneralDetails(phone)
                val details = response.generalDetails
                sharedPreferences.edit().apply {
                    putString("firstName", details.firstName)
                    putString("lastName", details.lastName)
                    // Safely format date, handle potential nulls or invalid timestamps
                    try {
                        val date = Date(details.dob._seconds * 1000)
                        putString("dob", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
                    } catch (e: Exception) {
                        Log.w("fetchProfileDetails", "Could not parse date: ${details.dob._seconds}")
                    }
                    putString("phoneNumber", details.phone)
                    putString("city", details.city)
                    putString("secondaryNumber", details.secondaryNumber)
                    putString("bloodGroup", details.bloodGroup)
                    putString("fatherName", details.fatherName)
                    putString("address", details.address)
                    putString("languageKnown", details.languageKnown.joinToString(","))
                    putString("img", details.image)
                    apply()
                }
            } catch (e: Exception) {
                Log.e("fetchProfileDetails", "Retrofit Error: ${e.message}", e)
            }
        }
    }
}