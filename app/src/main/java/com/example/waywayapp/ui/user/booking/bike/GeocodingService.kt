package com.example.waywayapp.ui.user.booking.bike

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("search")
    suspend fun searchAddress(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countryCodes: String = "vn"
    ): List<GeocodingResponse>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): ReverseGeocodingResponse
}

data class GeocodingResponse(
    val display_name: String,
    val lat: String,
    val lon: String
)

data class ReverseGeocodingResponse(
    val display_name: String
)
