package com.project.detectedfacerecognation.api
import android.content.Context
object UtilsApi {
//    val BASE_URL_API = "http://157.245.200.237:8000/"
//
//    // Mendeklarasikan Interface BaseApiService
//    fun getAPIService(): ApiRest? {
//        val apiClient = ApiClient()
//        return apiClient.getClient(BASE_URL_API)?.create(ApiRest::class.java)
//    }
    private const val BASE_URL_V1 = "http://157.245.200.237:8000/"
    private const val BASE_URL_V2 = "https://face-capture.ap.ngrok.io/"

    fun getAPIService(context: Context): ApiRest? {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val apiVersion = sharedPref.getString("API_VERSION", "v2")  // Default Versi 2

        val baseUrl = if (apiVersion == "v2") BASE_URL_V2 else BASE_URL_V1

        val apiClient = ApiClient()
        return apiClient.getClient(baseUrl)?.create(ApiRest::class.java)
    }
}