package com.eggbucket.b2c_delivery_app

import com.google.firebase.firestore.auth.User
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @Multipart
    @POST("/api/v1/deliveryPartner/personalDocs/aadharcard/{deliveryPartnerId}")
    fun uploadAadharDetails(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/deliveryPartner/personalDocs/pancard/{deliveryPartnerId}")
    fun uploadPanDetails(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/deliveryPartner/personalDocs/dl/{deliveryPartnerId}")
    fun uploadDLDetails(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>



    @Multipart
    @POST("/api/v1/deliveryPartner/vehicleDetails/{deliveryPartnerId}")
    fun uploadVehicleDocument(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part img: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/deliveryPartner/vehicleDetails/{deliveryPartnerId}")
    fun uploadPassbookDocument(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part img: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("/api/v1/deliveryPartner/profile/{phone}")
    suspend fun getGeneralDetails(
        @Path("phone") phone: String
    ): ApiResponse


    @PATCH("/api/v1/deliveryPartner/markorderdelivered/{phone}/{orderId}")
    fun markOrderDelivered(
        @Path("phone") phone: String,
        @Path("orderId") orderId: String
    ): Call<Void>
}
