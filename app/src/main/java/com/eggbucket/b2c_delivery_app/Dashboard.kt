package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val orderRequestLayout = view.findViewById<LinearLayout>(R.id.Order_request)


        orderRequestLayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_deliveredOrders)
        }
    }
}
