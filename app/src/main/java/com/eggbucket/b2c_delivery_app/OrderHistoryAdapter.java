package com.eggbucket.b2c_delivery_app; // Use your actual package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<OrderHistoryModel> orderHistoryModelList;

    // Constructor to pass the list of orders
    public OrderHistoryAdapter(List<OrderHistoryModel> orderHistoryModelList) {
        this.orderHistoryModelList = orderHistoryModelList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the order_item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // Get the current order from the list
        OrderHistoryModel orderHistoryModel = orderHistoryModelList.get(position);

        // Set the data in the views
        holder.productImage1.setImageResource(orderHistoryModel.getProductImages().get(0));
        holder.productImage2.setImageResource(orderHistoryModel.getProductImages().get(1));

        // Handle additional images if present
        if (orderHistoryModel.getProductImages().size() > 2) {
            holder.extraProductsText.setVisibility(View.VISIBLE);
            holder.extraProductsText.setText("+" + (orderHistoryModel.getProductImages().size() - 2));
        } else {
            holder.extraProductsText.setVisibility(View.GONE);
        }

        holder.orderDate.setText("Order Placed on " + orderHistoryModel.getDate());
        holder.orderStatus.setText(orderHistoryModel.getStatus());
        holder.orderAmt.setText("₹ " + orderHistoryModel.getAmount());
        holder.orderId.setText("Order ID: " + orderHistoryModel.getId());
    }

    @Override
    public int getItemCount() {
        return orderHistoryModelList.size();
    }

    // ViewHolder class to represent each order item in the RecyclerView
    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage1, productImage2;
        TextView extraProductsText, orderDate, orderStatus, orderAmt, orderId;
        CardView orderCard;

        public OrderViewHolder(View itemView) {
            super(itemView);

            // Initialize views
            productImage1 = itemView.findViewById(R.id.productImage1);
            productImage2 = itemView.findViewById(R.id.productImage2);
            extraProductsText = itemView.findViewById(R.id.extraProductsText);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderAmt = itemView.findViewById(R.id.orderAmt);
            orderId = itemView.findViewById(R.id.orderId);
        }
    }
}
