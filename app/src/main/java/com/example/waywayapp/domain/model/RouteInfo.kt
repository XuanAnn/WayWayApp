package com.example.waywayapp.domain.model

import com.google.android.gms.maps.model.LatLng

data class RouteInfo(
    val polylinePoints: List<LatLng>,
    val distanceMeters: Double,
    val durationSeconds: Double
)