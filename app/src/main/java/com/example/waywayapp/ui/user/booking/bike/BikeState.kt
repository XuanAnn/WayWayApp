package com.example.waywayapp.ui.user.booking.bike

import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.model.LatLng

data class BikeState(
    val currentLatLng: LatLng? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val polylinePoints: List<LatLng> = emptyList(),

    val pickupAddress: String = "Vị trí hiện tại",
    val dropoffAddress: String = "",

    val price: Double = 0.0,
    val distance: String = "",
    val duration: String = "",

    val status: BookingStatus = BookingStatus.IDLE,
    val isLoading: Boolean = false,
    val error: String? = null,

    val promo: Double = 1.0,
    val driverName: String = "",
    val driverPhone: String = "",
    val driverPlate: String = "",
    val driverRating: Double = 0.0,
    val driverLatLng: LatLng? = null,
    val ridePhase: String = "",
    val rideStatus: String = "",
    val etaToPickup: String = "",
    val etaToDropoff: String = "",

    val promoCode: String = "Áp mã",
    val discount: Double = 0.0,
    val finalPrice: Double = 0.0,
    val currentRideId: String? = null,

    val momoOrderId: String? = null,
    val momoPayUrl: String? = null,
    val momoStatus: String = "IDLE",
    val momoMessage: String? = null
) {
    val canConfirmRide: Boolean
        get() = pickupLatLng != null &&
                dropoffLatLng != null &&
                price > 0.0
}
