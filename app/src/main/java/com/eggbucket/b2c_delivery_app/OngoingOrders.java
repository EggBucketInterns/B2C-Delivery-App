package com.eggbucket.b2c_delivery_app;

import static androidx.navigation.Navigation.findNavController;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OngoingOrders extends Fragment {

    private RecyclerView ordersRecyclerView;
    private OngoingOrdersAdapter ordersAdapter;

    public OngoingOrders() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ongoing_orders, container, false);



        // Apply WindowInsets to handle status bar spacing
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int statusBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
                }
            }
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        // Handle back icon click
        View backIcon = view.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            NavHostFragment.findNavController(OngoingOrders.this)
                    .navigate(R.id.action_deliveredOrders_to_dashboardFragment);
        });

//        for testing Purposes
//        TextView dateText = view.findViewById(R.id.dateText);
//        dateText.setOnClickListener(v -> {
//            NavHostFragment.findNavController(OngoingOrders.this)
//                    .navigate(R.id.action_deliveredOrders_to_newOrder);
//        });

        // Set up RecyclerView
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the data and set the adapter
        List<OngoingOrdersModel> orders = generateDummyOrders();
        ordersAdapter = new OngoingOrdersAdapter(orders);
        ordersRecyclerView.setAdapter(ordersAdapter);


        return view;
    }


    // Generate dummy data for testing
    private List<OngoingOrdersModel> generateDummyOrders() {
        List<OngoingOrdersModel> orders = new ArrayList<>();
        orders.add(new OngoingOrdersModel("#1101", "Pickup Successful", "500"));
        orders.add(new OngoingOrdersModel("#1102", "Pickup Pending", "1000"));
        orders.add(new OngoingOrdersModel("#1103", "Pickup Pending", "750"));
        return orders;
    }
}
