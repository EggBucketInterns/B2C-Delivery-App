package com.eggbucket.b2c_delivery_app

data class Profile(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val drivingLicence: String = "",
    val aadhaar: String = ""
)
