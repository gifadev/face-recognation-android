package com.project.detectedfacerecognation.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiRest {

//    @GET("api/data")
//    fun getData(): Call<ArrayList<Kategori>>

    @Multipart
    @POST("search/")  // Untuk versi 1
    fun sendPictureV1(
        @Part photo: MultipartBody.Part?
    ): Call<JsonObject?>?

    @FormUrlEncoded
    @POST("analyze64/")  // Endpoint baru
    fun sendPictureV2(
        @Field("img64") img64: String  // Field untuk Base64 image
    ): Call<JsonObject?>?

    @POST("/api/biometric-data") // Endpoint API send to vps
    fun sendBiometricData(
        @Body data: JsonObject
    ): Call<JsonObject>
}