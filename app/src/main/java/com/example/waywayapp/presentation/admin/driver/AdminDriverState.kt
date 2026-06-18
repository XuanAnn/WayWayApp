package com.example.waywayapp.presentation.admin.driver

import com.example.waywayapp.domain.model.AdminPromo
import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser
import com.example.waywayapp.domain.model.Ride

enum class AdminSection {
    USERS,
    DRIVERS,
    PROMOS,
    RIDES
}
enum class DiscountType(val id: String, val title: String) {
    PERCENT("percent", "Phần trăm (%)"),
    FIXED("fixed", "Số tiền cố định (VNĐ)")
}

enum class SystemServiceType(val id: String, val title: String) {
    BIKE("bike", "Xe máy"),
    CAR("car", "Ô tô 4 chỗ"),
    DELIVERY("delivery", "Giao hàng")
}

data class AdminDriverState(
    val users: List<AdminUser> = emptyList(),
    val drivers: List<AdminDriver> = emptyList(),
    val promos: List<AdminPromo> = emptyList(),
    val rides: List<Ride> = emptyList(),
    val editingUser: AdminUser = AdminUser(),
    val editingDriver: AdminDriver = AdminDriver(),
    val editingPromo: AdminPromo = AdminPromo(),
    val editingRide: Ride = Ride(),
    val selectedSection: AdminSection = AdminSection.USERS,
    val searchQuery: String = "",
    val isUserDetailOpen: Boolean = false,
    val isDriverDetailOpen: Boolean = false,
    val isPromoDetailOpen: Boolean = false,
    val isRideDetailOpen: Boolean = false,
    val isLoadingUsers: Boolean = true,
    val isLoadingDrivers: Boolean = true,
    val isLoadingPromos: Boolean = true,
    val isLoadingRides: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null
)
