package com.example.waywayapp.ui.user.booking.bike

import com.google.android.gms.maps.model.LatLng

data class BikeState(
    val status: BookingStatus = BookingStatus.SELECTING,
    val currentLatLng: LatLng? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val polylinePoints: List<LatLng> = emptyList(),
    val pickupAddress: String = "Vị trí của bạn",
    val dropoffAddress: String = "",
    val price: Double = 0.0,
    val distance: String = "",
    val duration: String = "",
    val isFindingDriver: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class BookingStatus {
    SELECTING,
    WAITING,
    ON_TRIP,
    COMPLETED
}