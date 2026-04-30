package com.example.waywayapp.ui.user.booking.bike.viewmodel

import com.google.android.gms.maps.model.LatLng

enum class BookingStatus {
    IDLE,        // Màn hình chọn điểm đến
    FINDING,     // Đang tìm tài xế
    ON_TRIP,     // Đang trong chuyến đi
    COMPLETED    // Chuyến đi hoàn thành
}

data class BikeState(
    val currentLatLng: LatLng? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val polylinePoints: List<LatLng> = emptyList(),
    val pickupAddress: String = "Đang xác định vị trí...",
    val dropoffAddress: String = "",
    val price: Double = 0.0,
    val distance: String = "",
    val duration: String = "",
    val status: BookingStatus = BookingStatus.IDLE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val promo: Double = 0.0,
    // Driver info (Mock)
    val driverName: String = "Nguyễn Văn A",
    val driverPlate: String = "29-A1 123.45",
    val driverRating: Double = 4.9,
    val promoCode: String = "",
    val discount: Double = 0.0,
    val finalPrice: Double = 0.0
)
