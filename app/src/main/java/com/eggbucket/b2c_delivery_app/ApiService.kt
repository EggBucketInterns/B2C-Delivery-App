package com.eggbucket.b2c_delivery_app

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("drivers/{driverId}/details")
    suspend fun getGeneralDetails(
        @Path("driverId") driverId: String
    ): ApiResponse
    @GET("api/v1/customer/user/{phoneno}")
    fun getCustomername(@Path("phoneno") phoneNo: String): Call<JsonObject>
}
