package com.eggbucket.b2c_delivery_app;

import static androidx.navigation.Navigation.findNavController;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OngoingOrders extends Fragment {

    private static final String API_URL = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/0987654321";
    private RecyclerView ordersRecyclerView;
    private OngoingOrdersAdapter ordersAdapter;

    private View loaderContainer;
    private ProgressBar loader;
    private TextView loaderText;

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

        // Initialize views
        loaderContainer = view.findViewById(R.id.loaderContainer);
        loader = view.findViewById(R.id.loader);
        loaderText = view.findViewById(R.id.loaderText);
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Show loader initially
        showLoader();

        // Fetch orders dynamically from the API
        fetchOrders();

        return view;
    }

    // Show loader and hide RecyclerView
    private void showLoader() {
        loaderContainer.setVisibility(View.VISIBLE);
        loaderText.setText("Fetching Live Orders...");
        ordersRecyclerView.setVisibility(View.GONE);
    }

    // Hide loader and show RecyclerView
    private void hideLoader() {
        loaderContainer.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.VISIBLE);
    }

    // Fetch orders from API using OkHttp
    private void fetchOrders() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API Error", "Failed to fetch orders: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    loaderText.setText("Failed to fetch orders. Please try again.");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray pendingOrders = jsonResponse.getJSONArray("pendingOrders");

                        List<OngoingOrdersModel> orders = new ArrayList<>();
                        for (int i = 0; i < pendingOrders.length(); i++) {
                            JSONObject order = pendingOrders.getJSONObject(i);
                            String orderId = order.getString("orderId");
                            String status = "Pickup Pending"; // Set a default status for now
                            String amount = order.getString("amount"); // Get the amount dynamically
                            JSONObject outletInfo = order.getJSONObject("outletInfo");
                            JSONObject deliveryAddress = order.getJSONObject("deliveryAddress");
                            JSONObject customerInfo= order.getJSONObject("customerInfo");
                            JSONObject products = order.getJSONObject("products");
                            OngoingOrdersModel ongoingOrder = new OngoingOrdersModel(
                                    orderId, status, amount, outletInfo,
                                    deliveryAddress, products, customerInfo
                            );
                            orders.add(ongoingOrder);

                        }

                        // Update UI on the main thread
                        getActivity().runOnUiThread(() -> {
                            hideLoader();
                            ordersAdapter = new OngoingOrdersAdapter(orders, getContext());
                            ordersRecyclerView.setAdapter(ordersAdapter);
                        });

                    } catch (Exception e) {
                        Log.e("JSON Error", "Error parsing orders: " + e.getMessage());
                        getActivity().runOnUiThread(() -> {
                            loaderText.setText("Failed to load orders. Please try again.");
                        });
                    }
                } else {
                    Log.e("API Error", "Failed to fetch orders: " + response.message());
                    getActivity().runOnUiThread(() -> {
                        loaderText.setText("Failed to fetch orders. Please try again.");
                    });
                }
            }
        });
    }
}
