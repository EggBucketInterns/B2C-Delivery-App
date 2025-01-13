package com.eggbucket.b2c_delivery_app

import org.osmdroid.util.GeoPoint

data class GeneralDetails(
    val bloodGroup: String,
    val fatherName: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address: Address,
    val city: String,
    val secondaryNumber: String,
    val dob: Timestamp,
    val languageKnown: List<String>,
    val updatedAt: Timestamp,
    val image: String
)
data class Address(
    val fullAddress: fullAddress,
    val coordinates: GeoPoint
)
data class fullAddress(
    val addressLine1: String?,
    val addressLine2: String?,
    val area: String?,
    val city: String?,
    val country: String?,
    val state: String?,
    val flatNo: String?,

    val zip: String?

)

data class TotalOrders(
    val count: Int,
    val orders: List<Order>
)

data class Order(
    val id: String
)

data class ApiResponse(
    val id: String,
    val generalDetails: GeneralDetails,
    
)

data class Timestamp(
    val _seconds: Long,
    val _nanoseconds: Int
)
