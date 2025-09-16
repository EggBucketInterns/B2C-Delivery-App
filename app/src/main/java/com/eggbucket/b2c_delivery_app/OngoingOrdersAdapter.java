package com.eggbucket.b2c_delivery_app;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OngoingOrdersAdapter extends RecyclerView.Adapter<OngoingOrdersAdapter.OrderViewHolder> {

    private final List<OngoingOrdersModel> ordersList;
    private final Context context;

    private static final String PREFS_NAME = "OrderPrefs";
    private static final String DATA_KEY = "SelectedOrderData";

    public OngoingOrdersAdapter(List<OngoingOrdersModel> ordersList, Context context) {
        this.ordersList = ordersList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delivery_order_item, parent, false);
        return new OrderViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
//        OngoingOrdersModel order = ordersList.get(position);
//
//        // Display order details
//        holder.orderNumber.setText(order.getOrderNumber());
//        holder.orderStatus.setText(order.getStatus());
//        holder.orderValue.setText("Order Value: ₹ " + order.getOrderValue());
//
//        // Handle item click to save data
//        holder.itemView.setOnClickListener(v -> {
//            saveOrderDataToSharedPreferences(order);  // Save data to SharedPreferences
//            String savedOrderData = getOrderDataFromSharedPreferences();  // Retrieve saved data
//
//            // Log saved data for debugging
//            Log.d("SharedPreferencesData", savedOrderData);
//
//            NavController navController = Navigation.findNavController(v);
//            navController.navigate(R.id.action_deliveredOrders_to_pickupMap);
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OngoingOrdersModel order = ordersList.get(position);

        // Bind values
        holder.orderNumber.setText("Order #" + order.getOrderNumber());
        holder.orderStatus.setText("Status:" +order.getStatus());
        holder.orderValue.setText("Order Value: ₹ " + order.getOrderValue());

        // Click on "Pickup Order" → open Pickup Map
        holder.pickupText.setOnClickListener(v -> {
            saveOrderDataToSharedPreferences(order);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_dashboardFragment_to_pickupMap);
        });

        // Click on whole card → open Order Details
        holder.itemView.setOnClickListener(v -> {
            saveOrderDataToSharedPreferences(order);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_dashboardFragment_to_orderDetails);
        });
    }


    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderStatus, orderValue, pickupText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderValue = itemView.findViewById(R.id.orderValue);
            pickupText = itemView.findViewById(R.id.pickupText);
        }
    }

    /**
     * Save selected order data in SharedPreferences
     */
    private void saveOrderDataToSharedPreferences(OngoingOrdersModel order) {
        try {
            // Convert order details to JSONObject
            JSONObject data = new JSONObject();
            data.put("orderNumber", order.getOrderNumber());
            data.put("status", order.getStatus());
            data.put("orderValue", order.getOrderValue());
            data.put("outletInfo", order.getOutletInfo());  // Store the outlet info as JSON
            data.put("deliveryAddress", order.getDeliveryAddress());  // Store the delivery address as JSON
            data.put("products", order.getProducts());
            data.put("customerInfo", order.getCustomerInfo());

            // Save the updated data back to SharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DATA_KEY, data.toString());
            editor.apply();

            Log.d("SharedPreferencesData", "Data saved: " + data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SaveOrderDataError", "Failed to save order data: " + e.getMessage());
        }
    }

    /**
     * Get saved order data from SharedPreferences
     */
    private String getOrderDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DATA_KEY, "No Data Found");
    }
}
