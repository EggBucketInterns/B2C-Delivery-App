package com.eggbucket.b2c_delivery_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class DeliveredOrders extends Fragment {

    public DeliveredOrders() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ongoing_orders, container, false);


        View backIcon = view.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            NavHostFragment.findNavController(DeliveredOrders.this)
                    .navigate(R.id.action_deliveredOrders_to_dashboardFragment);
        });

        return view;
    }
}
