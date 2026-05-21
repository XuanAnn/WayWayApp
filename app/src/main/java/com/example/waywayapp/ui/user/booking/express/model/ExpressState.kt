package com.example.waywayapp.ui.user.booking.express.model

import com.google.android.gms.maps.model.LatLng

data class ExpressState(
    val pickupAddress: String = "Chọn điểm lấy hàng",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,

    val dropoffAddress: String = "Giao đến đâu?",
    val dropoffLat: Double = 0.0,
    val dropoffLng: Double = 0.0,

    val packageDetail: String = "",
    val codEnabled: Boolean = false,
    val handDelivery: Boolean = false,
    val bigPackage: Boolean = false,

    val routePoints: List<LatLng> = emptyList(),
    val distanceKm: Double = 0.0,
    val durationMinute: Double = 0.0,

    val basePrice: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val extraFee: Double
        get() {
            var fee = 0.0
            if (codEnabled) fee += 5000.0
            if (handDelivery) fee += 10000.0
            if (bigPackage) fee += 15000.0
            return fee
        }

    val totalPrice: Double
        get() = basePrice + extraFee

    val canCheckOrder: Boolean
        get() = pickupLat != 0.0 &&
                pickupLng != 0.0 &&
                dropoffLat != 0.0 &&
                dropoffLng != 0.0 &&
                packageDetail.isNotBlank()
}