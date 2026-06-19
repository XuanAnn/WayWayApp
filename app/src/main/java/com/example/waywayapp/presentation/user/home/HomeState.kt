package com.example.waywayapp.presentation.user.home

import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.domain.model.Driver
import com.example.waywayapp.domain.model.Service
import com.example.waywayapp.domain.model.User
import com.example.waywayapp.presentation.user.home.model.ServiceUI

data class HomeState(
    val currentUser: User? = null,
    val currentDriver: Driver? = null,
    val searchText: String = "",
    val searchSuggestions: List<GeocodingResponseDto> = emptyList(),
    val services: List<ServiceUI> = emptyList(),
    val momoLinked: Boolean = false,
    val momoPhone: String = "",
    val momoDisplayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
