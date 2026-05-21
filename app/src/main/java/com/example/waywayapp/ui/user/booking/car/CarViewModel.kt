package com.example.waywayapp.ui.user.booking.car

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.example.waywayapp.ui.user.booking.car.model.CarLocationType
import com.example.waywayapp.ui.user.booking.car.model.CarState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.ceil

class CarViewModel : ViewModel() {

    private val TAG = "WayWay_CarVM"

    private val _uiState = MutableStateFlow(CarState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GeocodingResponseDto>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val geocodingApi = RetrofitProvider.geocodingApi
    private val osrmApi = RetrofitProvider.osrmApi

    private lateinit var geocoder: Geocoder

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length > 2 }
                .distinctUntilChanged()
                .collect { query ->
                    performAutocomplete(query)
                }
        }
    }

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
        _uiState.update {
            it.copy(dropoffAddress = value)
        }
    }

    private suspend fun performAutocomplete(query: String) {
        try {
            val results = geocodingApi.searchAddress(query = query)
            _searchResults.value = results
        } catch (e: Exception) {
            Log.e(TAG, "Autocomplete error: ${e.message}")
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    @SuppressLint("MissingPermission")
    fun initLocation(context: Context) {
        geocoder = Geocoder(context, Locale("vi", "VN"))

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)

                _uiState.update {
                    it.copy(
                        currentLatLng = latLng,
                        pickupLatLng = latLng
                    )
                }

                getAddressFromLatLng(latLng) { address ->
                    _uiState.update {
                        it.copy(pickupAddress = address)
                    }
                }
            }
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val results = geocodingApi.searchAddress(query = query)

                if (results.isNotEmpty()) {
                    val result = results.first()
                    val latLng = LatLng(
                        result.lat.toDouble(),
                        result.lon.toDouble()
                    )

                    _uiState.update {
                        it.copy(
                            dropoffLatLng = latLng,
                            dropoffAddress = result.display_name,
                            isLoading = false,
                            error = null
                        )
                    }

                    calculateRoute()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không tìm thấy địa điểm"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi tìm kiếm địa điểm"
                    )
                }
            }
        }
    }

    fun setCarLocationFromMap(
        type: CarLocationType,
        latLng: LatLng
    ) {
        when (type) {
            CarLocationType.PICKUP -> {
                _uiState.update {
                    it.copy(
                        pickupLatLng = latLng,
                        pickupAddress = "Đang lấy địa chỉ..."
                    )
                }

                getAddressFromLatLng(latLng) { address ->
                    _uiState.update {
                        it.copy(pickupAddress = address)
                    }

                    calculateRoute()
                }
            }

            CarLocationType.DROPOFF -> {
                _uiState.update {
                    it.copy(
                        dropoffLatLng = latLng,
                        dropoffAddress = "Đang lấy địa chỉ..."
                    )
                }

                getAddressFromLatLng(latLng) { address ->
                    _uiState.update {
                        it.copy(dropoffAddress = address)
                    }

                    calculateRoute()
                }
            }
        }
    }

    fun calculateRoute() {
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val coordinates =
                    "${pickup.longitude},${pickup.latitude};${dropoff.longitude},${dropoff.latitude}"

                val response = osrmApi.getRoute(coordinates)

                if (response.routes.isNotEmpty()) {
                    val route = response.routes.first()

                    val distanceKm = route.distance / 1000.0
                    val durationMinute = route.duration / 60.0
                    val price = calculateCarPrice(distanceKm)

                    _uiState.update {
                        it.copy(
                            polylinePoints = PolyUtil.decode(route.geometry),
                            distance = String.format("%.1f km", distanceKm),
                            duration = String.format("%.0f phút", durationMinute),
                            price = price,
                            finalPrice = price,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không tìm thấy lộ trình"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Route error", e)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi tính tuyến đường"
                    )
                }
            }
        }
    }

    private fun calculateCarPrice(distanceKm: Double): Double {
        val firstTwoKmPrice = 36_000.0

        if (distanceKm <= 2.0) {
            return firstTwoKmPrice
        }

        val extraKm = ceil(distanceKm - 2.0)

        return firstTwoKmPrice + extraKm * 8_000.0
    }

    fun startBooking() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = BookingStatus.FINDING)
            }

            delay(3000)

            _uiState.update {
                it.copy(status = BookingStatus.ON_TRIP)
            }
        }
    }

    fun cancelBooking() {
        _uiState.update {
            it.copy(status = BookingStatus.IDLE)
        }
    }

    fun resetBooking() {
        _uiState.update {
            CarState(
                currentLatLng = it.currentLatLng,
                pickupLatLng = it.currentLatLng,
                pickupAddress = it.pickupAddress
            )
        }
    }

    private fun getAddressFromLatLng(
        latLng: LatLng,
        onResult: (String) -> Unit
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                ) { addresses ->
                    onResult(
                        addresses.firstOrNull()?.let {
                            formatAddress(it)
                        } ?: "Vị trí không xác định"
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses =
                    geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )

                onResult(
                    addresses?.firstOrNull()?.let {
                        formatAddress(it)
                    } ?: "Vị trí không xác định"
                )
            }
        } catch (e: Exception) {
            onResult("Vị trí không xác định")
        }
    }

    private fun formatAddress(addr: Address): String {
        return addr.getAddressLine(0)
            ?: addr.featureName
            ?: "Vị trí không xác định"
    }
}