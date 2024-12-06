package com.eggbucket.b2c_delivery_app

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
    @POST("api/v1/deliveryPartner/personalDocs/aadharcard/{phoneNumber}")
    fun uploadAadharDetails(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @POST("api/v1/deliveryPartner/personalDocs/pancard/{phoneNumber}")
    fun uploadPanDetails(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/deliveryPartner/personalDocs/dl/{phoneNumber}")
    fun uploadDLDetails(
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

    @Multipart
    @POST("/api/v1/deliveryPartner/personalInformation")
    fun submitPersonalDetails(
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("fatherName") fatherName: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("secondaryNumber") secondaryNumber: RequestBody,
        @Part("bloodGroup") bloodGroup: RequestBody,
        @Part("city") city: RequestBody,
        @Part("address") address: RequestBody,
        @Part("languageKnown") languageKnown: RequestBody,
        @Part img: MultipartBody.Part
    ): Call<ResponseBody>

}
