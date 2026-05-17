package com.example.waywayapp.core.network

import com.example.waywayapp.data.remote.api.AuthApi
import com.example.waywayapp.data.remote.api.GeocodingApi
import com.example.waywayapp.data.remote.api.OsrmApi

object RetrofitProvider {

    val authApi: AuthApi by lazy {

        RetrofitFactory.create(
            ApiConstants.AUTH_BASE_URL
        ).create(AuthApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {

        RetrofitFactory.create(
            ApiConstants.NOMINATIM_BASE_URL
        ).create(GeocodingApi::class.java)
    }

    val osrmApi: OsrmApi by lazy {

        RetrofitFactory.create(
            ApiConstants.OSRM_BASE_URL
        ).create(OsrmApi::class.java)
    }
}