package com.example.waywayapp.presentation.user.history

import com.example.waywayapp.domain.model.Ride

data class HistoryState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val error: String? = null
)
