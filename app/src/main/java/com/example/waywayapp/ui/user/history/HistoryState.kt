package com.example.waywayapp.ui.user.history

import com.example.waywayapp.data.model.Ride

data class HistoryState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val error: String? = null
)
