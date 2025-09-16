package com.eggbucket.b2c_delivery_app

data class OrderDashboardModel(
    val orderId: String,
    val orderDate: String,
    val orderAmount: String,
    val orderStatus: String,
    val productImages: List<Int>, // drawable resource IDs
    val extraProductsCount: Int
)
