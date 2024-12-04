package com.eggbucket.b2c_delivery_app;

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
import androidx.annotation.Nullable;
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

public class OrderHistory extends Fragment {

    private static final String API_URL = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/fetchOrders/0987654321";
    private RecyclerView recyclerViewOrders;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<OrderHistoryModel> orderHistoryModelList;
    private View loaderContainer;
    private ProgressBar loader;
    private TextView loaderText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);




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

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loaderContainer = view.findViewById(R.id.loaderContainer);
        loader = view.findViewById(R.id.loader);
        loaderText = view.findViewById(R.id.loaderText);

        orderHistoryModelList = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(orderHistoryModelList);
        recyclerViewOrders.setAdapter(orderHistoryAdapter);

        view.findViewById(R.id.backIcon).setOnClickListener(v ->
                NavHostFragment.findNavController(OrderHistory.this)
                        .navigate(R.id.action_orderSummary_to_dashboardFragment));

        // Show loader initially
        loaderContainer.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);

        // Fetch orders
        fetchOrderHistory();

        return view;
    }

    private void fetchOrderHistory() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
                requireActivity().runOnUiThread(() -> loaderContainer.setVisibility(View.GONE));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray ordersArray = jsonResponse.getJSONArray("orders");

                        orderHistoryModelList.clear();

                        for (int i = 0; i < ordersArray.length(); i++) {
                            JSONObject orderObject = ordersArray.getJSONObject(i);

                            String orderId = orderObject.getString("id");
                            JSONObject orderDateObject = orderObject.getJSONObject("orderDate");
                            String orderDate = convertFirestoreTimestampToDate(
                                    orderDateObject.getLong("_seconds"),
                                    orderDateObject.getInt("_nanoseconds")
                            );
                            String deliveryStatus = orderObject.getBoolean("deliveredStatus") ? "Delivered" : "Pending";
                            String price = String.valueOf(orderObject.getInt("price"));

                            List<Integer> productImages = new ArrayList<>();
                            productImages.add(R.drawable.eggs_image_6);
                            productImages.add(R.drawable.eggs_image_30);

                            orderHistoryModelList.add(new OrderHistoryModel(orderDate, deliveryStatus, orderId, price, productImages));
                        }

                        requireActivity().runOnUiThread(() -> {
                            loaderContainer.setVisibility(View.GONE);
                            recyclerViewOrders.setVisibility(View.VISIBLE);
                            orderHistoryAdapter.notifyDataSetChanged();
                        });

                    } catch (Exception e) {
                        Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                    }
                } else {
                    Log.e("API_ERROR", "Request failed with status: " + response.code());
                    requireActivity().runOnUiThread(() -> loaderContainer.setVisibility(View.GONE));
                }
            }
        });
    }

    private String convertFirestoreTimestampToDate(long seconds, int nanoseconds) {
        long milliseconds = seconds * 1000 + nanoseconds / 1_000_000;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.util.Date date = new java.util.Date(milliseconds);
        return sdf.format(date);
    }
}
