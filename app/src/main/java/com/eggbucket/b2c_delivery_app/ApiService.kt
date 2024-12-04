package com.eggbucket.b2c_delivery_app

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

}