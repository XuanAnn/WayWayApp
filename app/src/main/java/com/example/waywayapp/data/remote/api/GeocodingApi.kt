package com.example.waywayapp.data.remote.api

import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.data.remote.dto.geocoding.ReverseGeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("search")
    suspend fun searchAddress(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countryCodes: String = "vn",
        @Query("accept-language") acceptLanguage: String = "vi",
        @Query("dedupe") dedupe: Int = 0
    ): List<GeocodingResponseDto>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): ReverseGeocodingResponseDto
}
