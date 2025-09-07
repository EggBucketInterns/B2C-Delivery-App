package com.eggbucket.b2c_delivery_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OngoingOrders extends Fragment {

    // Base URL without the dynamic part
    private static final String API_URL_BASE = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/getcurrentorders/";

    private RecyclerView ordersRecyclerView;
    private OngoingOrdersAdapter ordersAdapter;
    private View loaderContainer;
    private ProgressBar loader;
    private TextView loaderText;

    public OngoingOrders() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ongoing_orders, container, false);

        // Apply WindowInsets to handle status bar spacing if needed (standard boilerplate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
                v.setPadding(0, statusBarHeight, 0, 0);
                return insets;
            });
        }

        // Handle back icon click
        View backIcon = view.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(OngoingOrders.this)
                        .navigate(R.id.action_deliveredOrders_to_dashboardFragment);
            }
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

    private void showLoader() {
        loaderContainer.setVisibility(View.VISIBLE);
        loaderText.setText("Fetching Live Orders...");
        ordersRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoader() {
        loaderContainer.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.VISIBLE);
    }

    private void fetchOrders() {
        // --- START RECOMMENDED CHANGES ---

        // 1. Get SharedPreferences instance safely.
        if (getContext() == null) {
            Log.e("OngoingOrders", "Context is null, cannot fetch SharedPreferences.");
            return;
        }
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        // 2. Retrieve the logged-in user's ID.
        String phoneNo = sharedPreferences.getString("phone_no", null);

        // 3. Validate the ID.
        if (phoneNo == null) {
            Log.e("OngoingOrders", "User phone number not found in SharedPreferences!");
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    loaderText.setText("Session error. Please log in again.");
                });
            }
            return;
        }

        // 4. Construct the dynamic URL.
        String dynamicApiUrl = API_URL_BASE + phoneNo;

        // --- END RECOMMENDED CHANGES ---

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(dynamicApiUrl) // Use the dynamic URL here
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API Error", "Failed to fetch orders: " + e.getMessage());
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        loaderText.setText("Failed to fetch orders. Check network connection.");
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("API Error", "Failed response code: " + response.code());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            loaderText.setText("Failed to retrieve orders (Error " + response.code() + ").");
                        });
                    }
                    return;
                }

                final String responseData = response.body() != null ? response.body().string() : "";

                try {
                    JSONArray ordersArray = new JSONArray(responseData);
                    final List<OngoingOrdersModel> orders = new ArrayList<>();

                    for (int i = 0; i < ordersArray.length(); i++) {
                        JSONObject order = ordersArray.getJSONObject(i);

                        // Safely extract data from JSON object
                        String orderId = order.optString("orderId");
                        String amount = String.valueOf(order.optInt("amount", 0));
                        JSONObject outletInfo = order.optJSONObject("outletInfo");
                        JSONObject deliveryAddress = order.optJSONObject("deliveryAddress");
                        JSONObject customerInfo = order.optJSONObject("customerInfo");
                        JSONObject products = order.optJSONObject("products");

                        // TODO: Fetch real status instead of hardcoding, if available from API.
                        // For now, hardcoding based on the fact that these are pending orders.
                        String status = "Pickup Pending";

                        OngoingOrdersModel ongoingOrder = new OngoingOrdersModel(
                                orderId, status, amount, outletInfo,
                                deliveryAddress, products, customerInfo
                        );
                        orders.add(ongoingOrder);
                    }

                    // Update UI on the main thread
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            hideLoader();
                            if (orders.isEmpty()) {
                                loaderContainer.setVisibility(View.VISIBLE);
                                loaderText.setText("No ongoing orders found.");
                            } else {
                                ordersAdapter = new OngoingOrdersAdapter(orders, getContext());
                                ordersRecyclerView.setAdapter(ordersAdapter);
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e("JSON Error", "Error parsing orders: " + e.getMessage());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            loaderText.setText("An error occurred while loading data.");
                        });
                    }
                } finally {
                    response.close();
                }
            }
        });
    }
}