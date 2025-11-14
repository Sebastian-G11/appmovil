package com.example.firebase_testapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object   RetrofitClient {
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")//todas las llamadas utilizaran la url
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
