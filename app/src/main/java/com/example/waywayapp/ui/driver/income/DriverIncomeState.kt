package com.example.waywayapp.ui.driver.income

import com.example.waywayapp.data.model.Ride

data class DriverIncomeState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val error: String? = null
) {
    val completedTrips: Int
        get() = rides.size

    val totalIncome: Double
        get() = rides.sumOf { it.price }
}
