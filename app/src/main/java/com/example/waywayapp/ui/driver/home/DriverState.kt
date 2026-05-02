package com.example.waywayapp.ui.driver.home

import com.google.android.gms.maps.model.LatLng

enum class DriverStatus {
    OFFLINE,
    ONLINE,
    ON_THE_WAY_TO_PICKUP,
    ON_TRIP
}

data class DriverState(
    val status: DriverStatus = DriverStatus.OFFLINE,
    val currentLatLng: LatLng? = null,
    val currentEarnings: Double = 0.0,
    val currentTrips: Int = 0,
    val isLoading: Boolean = false,
    
    // Trip info if assigned
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val tripPrice: Double = 0.0,
    val polylinePoints: List<LatLng> = emptyList()
)
