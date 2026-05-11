package com.arz.store.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 is the special alias to your host loopback interface (localhost) from the Android emulator
    private const val BASE_URL = "https://arzstore.prjktla.my.id"
    private const val ALT_URL = ""

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
