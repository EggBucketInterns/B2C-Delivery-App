package com.eggbucket.b2c_delivery_app

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
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

    @GET("api/v1/deliveryPartner/profile/{phone}")
    fun getUserByPhone(@Path("phone") phone: String): Call<User>

    @Multipart
    @POST("/api/v1/deliveryPartner/vehicleDetails/{deliveryPartnerId}")
    fun uploadVehicleDocument(
        @Path("deliveryPartnerId") deliveryPartnerId: String,
        @Part img: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("/api/v1/deliveryPartner/profile/{phone}")
    suspend fun getGeneralDetails(
        @Path("phone") phone: String
    ): ApiResponse


    @GET("api/v1/customer/user/{phoneno}")
    fun getCustomername(@Path("phoneno") phoneNo: String): Call<JsonObject>
}
