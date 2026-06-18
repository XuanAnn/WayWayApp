package com.example.waywayapp.presentation.driver.income

import com.example.waywayapp.domain.model.Ride

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
