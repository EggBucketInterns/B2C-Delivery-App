package com.eggbucket.b2c_delivery_app;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderSummary extends Fragment {

    // Parameters for fragment arguments
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // Declare RecyclerView and Adapter
    private RecyclerView recyclerViewOrders;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<OrderHistoryModel> orderHistoryModelList;

    public OrderSummary() {
        // Required empty public constructor
    }

    public static OrderSummary newInstance(String param1, String param2) {
        OrderSummary fragment = new OrderSummary();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // Apply WindowInsets to handle status bar spacing
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int statusBarHeight = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
            } else {
                statusBarHeight = insets.getSystemWindowInsetTop();
            }
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        // Initialize RecyclerView and Adapter
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext())); // Vertical list

        orderHistoryModelList = new ArrayList<>();
        loadOrders(); // Load your order data (either hardcoded or from a database/API)

        orderHistoryAdapter = new OrderHistoryAdapter(orderHistoryModelList); // Adapter for the RecyclerView
        recyclerViewOrders.setAdapter(orderHistoryAdapter);

        // Back Icon Click Listener
        View backIcon = view.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrderSummary.this)
                    .navigate(R.id.action_orderSummary_to_dashboardFragment);
        });

        return view;
    }

    private void loadOrders() {
        // Example order data
        List<Integer> productImages = new ArrayList<>();
        productImages.add(R.drawable.eggs_image_6);  // Replace with actual drawable resources
        productImages.add(R.drawable.eggs_image_30);

        orderHistoryModelList.add(new OrderHistoryModel("01/01/2024", "Delivered", "12345", "500", productImages));
        orderHistoryModelList.add(new OrderHistoryModel("02/01/2024", "Pending", "12346", "750", productImages));
        // Add more orders here
    }
}
