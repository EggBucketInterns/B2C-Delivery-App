package com.eggbucket.b2c_delivery_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OngoingOrdersModel order = ordersList.get(position);
        holder.orderNumber.setText("Order No. " + order.getOrderNumber());
        holder.orderStatus.setText(order.getStatus());
        holder.orderValue.setText("Order Value: ₹ " + order.getOrderValue());

        holder.itemView.setOnClickListener(v -> {
            // Save order data to SharedPreferences
            saveOrderDataToSharedPreferences(order);

            // Get the saved order data from SharedPreferences
            String savedOrderData = getOrderDataFromSharedPreferences();

            // Show a Toast message with the saved order data
            Toast.makeText(context, "Saved Information: " + savedOrderData, Toast.LENGTH_LONG).show();

            // Optional: Log the saved order data for debugging purposes
            Log.d("SharedPreferencesData", savedOrderData);
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber, orderStatus, orderValue;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderValue = itemView.findViewById(R.id.orderValue);
        }
    }

    // Save selected order data in SharedPreferences
    private void saveOrderDataToSharedPreferences(OngoingOrdersModel order) {
        try {
            JSONObject orderData = new JSONObject();
            orderData.put("orderNumber", order.getOrderNumber());
            orderData.put("status", order.getStatus());
            orderData.put("orderValue", order.getOrderValue());
            orderData.put("outletName", order.getOutletName());
            orderData.put("outletAddress", order.getOutletAddress());
            orderData.put("outletPhone", order.getOutletPhone());
            orderData.put("deliveryAddress", order.getDeliveryAddress());
            orderData.put("products", order.getProducts());

            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DATA_KEY, orderData.toString());
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get saved order data from SharedPreferences
    private String getOrderDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DATA_KEY, "No Data Found");
    }
}
