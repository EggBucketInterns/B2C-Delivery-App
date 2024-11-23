package com.eggbucket.b2c_delivery_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import the Button class
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class NewOrder extends Fragment {

    public NewOrder() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_notification, container, false);


//        Button pickUpButton = view.findViewById(R.id.pickUpButton);
        RelativeLayout pickUpButton = view.findViewById(R.id.pickUpButton);
        pickUpButton.setOnClickListener(v -> {

            NavHostFragment.findNavController(NewOrder.this)
                    .navigate(R.id.action_newOrder_to_pickupMap);
        });

        return view;
    }
}
