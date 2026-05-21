package com.example.waywayapp.ui.user.booking.car.model

import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.model.LatLng

data class CarState(
    val currentLatLng: LatLng? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val polylinePoints: List<LatLng> = emptyList(),

    val pickupAddress: String = "Vị trí hiện tại",
    val dropoffAddress: String = "",

    val distance: String = "",
    val duration: String = "",
    val price: Double = 0.0,
    val finalPrice: Double = 0.0,

    val status: BookingStatus = BookingStatus.IDLE,
    val isLoading: Boolean = false,
    val error: String? = null,

    val driverName: String = "Trần Văn B",
    val driverPlate: String = "43A-888.88",
    val driverRating: Double = 4.8
) {
    val canConfirmRide: Boolean
        get() = pickupLatLng != null &&
                dropoffLatLng != null &&
                price > 0.0
}