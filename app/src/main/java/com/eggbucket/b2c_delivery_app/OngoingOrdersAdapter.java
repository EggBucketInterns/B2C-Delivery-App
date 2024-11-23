package com.eggbucket.b2c_delivery_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OngoingOrdersAdapter extends RecyclerView.Adapter<OngoingOrdersAdapter.OrderViewHolder> {

    private List<OngoingOrdersModel> ordersList;

    public OngoingOrdersAdapter(List<OngoingOrdersModel> ordersList) {
        this.ordersList = ordersList;
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
        holder.orderNumber.setText("Order No. "+order.getOrderNumber());
        holder.orderStatus.setText(order.getStatus());
        holder.orderValue.setText("Order Value: ₹ "+order.getOrderValue());

        // Optionally, you can set colors or styles based on status
        if (order.getStatus().equalsIgnoreCase("Pickup Successful")) {
            holder.orderStatus.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(android.R.color.holo_green_dark));
        } else if (order.getStatus().equalsIgnoreCase("Pickup Pending")) {
            holder.orderStatus.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(android.R.color.holo_red_dark));
        } else {
            holder.orderStatus.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(android.R.color.holo_red_dark));
        }
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
}
