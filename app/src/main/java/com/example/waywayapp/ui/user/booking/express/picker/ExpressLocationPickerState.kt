package com.example.waywayapp.ui.user.booking.express.picker

import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.google.android.gms.maps.model.LatLng

data class ExpressLocationPickerState(
    val currentLatLng: LatLng? = null,
    val selectedLatLng: LatLng? = null,
    val selectedAddress: String = "Đang xác định vị trí...",
    val searchQuery: String = "",
    val searchResults: List<GeocodingResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)