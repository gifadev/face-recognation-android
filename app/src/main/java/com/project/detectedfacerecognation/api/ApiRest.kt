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

    @Multipart
    @POST("analyze/")  // Untuk versi 2
    fun sendPictureV2(
        @Part photo: MultipartBody.Part?
    ): Call<JsonObject?>?
}