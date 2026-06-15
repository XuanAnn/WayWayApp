package com.example.waywayapp.ui.user.notification

import com.example.waywayapp.data.model.Ride

data class NotificationState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val error: String? = null
)
