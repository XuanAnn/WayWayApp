package com.example.waywayapp.data.remote.dto.osrm

data class OsrmRouteDto(
    val geometry: String,
    val distance: Double,
    val duration: Double
)