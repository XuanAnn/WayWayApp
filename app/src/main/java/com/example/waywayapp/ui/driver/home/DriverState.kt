package com.example.waywayapp.ui.driver.home

import com.example.waywayapp.data.model.Ride
import com.google.android.gms.maps.model.LatLng

enum class DriverStatus {
    OFFLINE,
    ONLINE,
    ON_THE_WAY_TO_PICKUP,
    ARRIVED_AT_PICKUP,
    ON_TRIP
}

data class DriverState(
    val status: DriverStatus = DriverStatus.OFFLINE,
    val currentLatLng: LatLng? = null,
    val currentEarnings: Double = 0.0,
    val currentTrips: Int = 0,
    val walletBalance: Double = 0.0,
    val walletCompletedTrips: Int = 0,
    val walletRides: List<Ride> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val driverName: String = "",
    val driverPhone: String = "",
    val driverPlate: String = "",
    
    // Trip info if assigned
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val passengerName: String = "",
    val passengerPhone: String = "",
    val tripPrice: Double = 0.0,
    val currentRideId: String? = null,
    val polylinePoints: List<LatLng> = emptyList(),
    val navigationTargetLatLng: LatLng? = null,
    val navigationTitle: String = "",
    val routeDistance: String = "",
    val routeDuration: String = "",
    val isRouting: Boolean = false
)
