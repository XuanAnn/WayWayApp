package com.example.waywayapp.presentation.user.notification

import com.example.waywayapp.domain.model.Ride

data class NotificationState(
    val isLoading: Boolean = true,
    val rides: List<Ride> = emptyList(),
    val error: String? = null
)
