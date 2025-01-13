package com.eggbucket.b2c_delivery_app

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.eggbucket.b2c_delivery_app.placeholder.PlaceholderContent.PlaceholderItem
import com.eggbucket.b2c_delivery_app.databinding.FragmentItemBinding


class MyNotificationRecyclerViewAdapter(
    private val values: MutableList<NotificationEntity>,
    private val onAccept: (NotificationEntity) -> Unit,
    private val onDelete: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<MyNotificationRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.orderIdView.text = "Id: ${item.orderId}"
        holder.statusView.text = "status: ${item.status}"
        holder.pickupView.text = "Pickup: ${item.pickup}"
        holder.deliveryView.text = "Delivery: ${item.delivery}"
        holder.orderValueView.text = "Value: ₹${item.orderValue}"
        holder.itemsView.text = "Items: E6(${item.itemE6}), E12(${item.itemE12}), E30(${item.itemE30})"

        holder.acceptButton.setOnClickListener {
            onAccept(item)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(item)
            values.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val orderIdView: TextView = binding.orderId
        val statusView: TextView = binding.status
        val pickupView: TextView = binding.pickup
        val deliveryView: TextView = binding.delivery
        val orderValueView: TextView = binding.orderValue
        val itemsView: TextView = binding.items
        val acceptButton: Button = binding.acceptButton
        val deleteButton: Button = binding.deleteButton
    }
}
