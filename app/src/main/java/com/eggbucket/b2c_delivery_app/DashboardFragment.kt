package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var completedOrdersCountTextView: TextView
    private lateinit var ongoingOrderCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the GraphView
        val graphView: GraphView = view.findViewById(R.id.graph)

        // Remove grid lines
        graphView.gridLabelRenderer.isHorizontalLabelsVisible = true
        graphView.gridLabelRenderer.isVerticalLabelsVisible = true
        graphView.gridLabelRenderer.setGridStyle(com.jjoe64.graphview.GridLabelRenderer.GridStyle.NONE)

        // Customize the X and Y axis labels
        graphView.gridLabelRenderer.horizontalAxisTitleColor = resources.getColor(R.color.orange) // Orange for X-axis
        graphView.gridLabelRenderer.verticalAxisTitleColor = resources.getColor(R.color.orange) // Orange for Y-axis
        graphView.gridLabelRenderer.textSize = 30f // Increase the size of the labels if necessary

        // Create a curvy LineGraphSeries (parabola-like curve)
        val series = LineGraphSeries<DataPoint>(generateCurvyDataPoints())
        series.color = resources.getColor(R.color.orange) // Set the graph line color to orange

        graphView.addSeries(series)

        // Reference the TextView for completed orders count
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

        // Fetch the order data and update the UI simultaneously
        fetchOrdersCount()
        fetchOngoingOrdersCount()
    }

    private fun fetchOrdersCount() {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/0987654321"

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

    private fun fetchOngoingOrdersCount() {
        val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/0987654321"

        val queue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val count = response.getInt("count")

                // Update the ongoing orders count TextView with the fetched data
                ongoingOrderCount.text = count.toString()

                // Once data is fetched, hide the loading drawable and show the actual data
                ongoingOrderCount.setBackgroundResource(0) // Remove the loading drawable
            },
            { error ->
                // Handle error (e.g., display a message or a default value)
                ongoingOrderCount.text = "0"

                // Remove the loading drawable in case of error as well
                ongoingOrderCount.setBackgroundResource(0)
            })

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest)
    }

    // Method to generate curvy (parabola-like) data points
    private fun generateCurvyDataPoints(): Array<DataPoint> {
        val dataPoints = mutableListOf<DataPoint>()
        for (i in 1..20) {
            val x = i.toDouble()
            // Generate a quadratic curve (parabola) with random fluctuation
            val y = Math.pow(x, 2.0) - 50 + Math.random() * 100 // Parabolic curve with some random fluctuation
            dataPoints.add(DataPoint(x, y))
        }
        return dataPoints.toTypedArray()
    }
}
