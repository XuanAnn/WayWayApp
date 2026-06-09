package com.example.waywayapp.ui.user.home

import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.ui.user.home.model.BannerUiModel
import com.example.waywayapp.ui.user.home.model.ServiceUiModel

data class HomeState(
    val searchText: String = "",
    val searchSuggestions: List<GeocodingResponseDto> = emptyList(),
    val services: List<ServiceUiModel> = emptyList(),
    val banners: List<BannerUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
