package com.project.detectedfacerecognation.api

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UtilsApi {
    private const val BASE_URL_V1 = "http://157.245.200.237:8000/"
    private const val BASE_URL_V2 = "https://face-capture.ap.ngrok.io/"
    private const val BASE_URL = "http://157.245.200.237/"

    fun getAPIService(context: Context): ApiRest {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val apiVersion = sharedPref.getString("API_VERSION", "v2")  // Default Versi 2

        // Pilih base URL berdasarkan versi API
        val baseUrl = when (apiVersion) {
            "v1" -> BASE_URL_V1
            "v2" -> BASE_URL_V2
            else -> BASE_URL
        }

        // Buat instance Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // Gunakan base URL yang dipilih
            .addConverterFactory(GsonConverterFactory.create()) // Tambahkan Gson converter
            .build()

        // Buat dan kembalikan instance ApiRest
        return retrofit.create(ApiRest::class.java)
    }
}