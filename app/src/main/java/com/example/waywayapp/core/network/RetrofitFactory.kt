package com.example.waywayapp.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    private val okHttpClient by lazy {

        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request =
                    chain.request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "WayWayApp/1.0"
                        )
                        .build()

                chain.proceed(request)
            }
            .build()
    }

    fun create(
        baseUrl: String
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }
}