package com.eggbucket.b2c_delivery_app

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("api/v1/deliveryPartner/personalDocs/aadharcard/{phoneNumber}")
    fun uploadAadharDetails(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @POST("api/v1/deliveryPartner/personalDocs/pancard/{phoneNumber}")
    fun uploadPanDetails(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @POST("/api/v1/deliveryPartner/personalDocs/dl/{phoneNumber}")
    fun uploadDLDetails(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @GET("api/v1/deliveryPartner/profile/{phone}")
    fun getUserByPhone(@Path("phone") phone: String): Call<User>

    @GET("api/v1/deliveryPartner/getoutletId/{phoneNumber}")
    fun getOutletId(
        @Path("phoneNumber") phoneNumber: String
    ): Call<PickupMap.OutletResponse>

}