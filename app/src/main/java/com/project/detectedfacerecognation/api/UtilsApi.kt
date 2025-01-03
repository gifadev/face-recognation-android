package com.project.detectedfacerecognation.api

object UtilsApi {
    val BASE_URL_API = "http://172.15.3.237:8000"

    // Mendeklarasikan Interface BaseApiService
    fun getAPIService(): ApiRest? {
        val apiClient = ApiClient()
        return apiClient.getClient(BASE_URL_API)?.create(ApiRest::class.java)
    }

    val BASE_URL_IMG_WISATA = BASE_URL_API + "api/wisata/"
}