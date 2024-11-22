package com.eggbucket.b2c_delivery_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.widget.Button

class DeliveryMapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivery_map, container, false)

        val viewOrderDetailsButton: Button = view.findViewById(R.id.view_order_details)

        viewOrderDetailsButton.setOnClickListener {
            val navController: NavController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_deliveryMapFragment2_to_orderDetails)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = DeliveryMapFragment()
    }
}
