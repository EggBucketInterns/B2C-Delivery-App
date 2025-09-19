package com.eggbucket.b2c_delivery_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderDashboardAdapter(
    private var orderList: List<OrderDashboardModel>,
    private val onItemClick: (OrderDashboardModel) -> Unit
) : RecyclerView.Adapter<OrderDashboardAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage1: ImageView = view.findViewById(R.id.productImage1)
        val productImage2: ImageView = view.findViewById(R.id.productImage2)
        val extraProductsText: TextView = view.findViewById(R.id.extraProductsText)
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val orderAmt: TextView = view.findViewById(R.id.orderAmt)
        val orderId: TextView = view.findViewById(R.id.orderId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.orderDate.text = "Order Placed on ${order.orderDate}"
        holder.orderStatus.text = order.orderStatus
        holder.orderAmt.text = "Rs. ${order.orderAmount}"
        holder.orderId.text = "Order ID: ${order.orderId}"

        // Product images
        if (order.productImages.isNotEmpty()) {
            holder.productImage1.setImageResource(order.productImages[0])
        }
        if (order.productImages.size > 1) {
            holder.productImage2.setImageResource(order.productImages[1])
        }
        if (order.extraProductsCount > 0) {
            holder.extraProductsText.visibility = View.VISIBLE
            holder.extraProductsText.text = "+${order.extraProductsCount}"
        } else {
            holder.extraProductsText.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    fun updateList(newList: List<OrderDashboardModel>) {
        orderList = newList
        notifyDataSetChanged()
    }
}
