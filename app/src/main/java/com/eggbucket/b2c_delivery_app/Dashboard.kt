package com.eggbucket.b2c_delivery_app

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle WindowInsets to avoid overlap
        view.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsets.Type.statusBars()).top
                } else {
                    insets.systemWindowInsetTop
                }
                v.setPadding(0, statusBarHeight, 0, 0)
            }
            insets
        }

        // Set up click listener for Order Request
        val orderRequestLayout = view.findViewById<LinearLayout>(R.id.Order_request)
        orderRequestLayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_deliveredOrders)
        }

        val orderCompleted = view.findViewById<LinearLayout>(R.id.total_orders_completed_layout)
        orderCompleted.setOnClickListener{
            findNavController().navigate(R.id.action_dashboardFragment_to_orderSummary)
        }
    }
}
