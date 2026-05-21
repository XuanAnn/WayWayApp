package com.example.waywayapp.ui.user.booking.express

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.example.waywayapp.ui.user.booking.express.model.ExpressState
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil

class ExpressViewModel : ViewModel() {

    private val TAG = "WayWay_ExpressVM"

    private val _uiState = MutableStateFlow(ExpressState())
    val uiState = _uiState.asStateFlow()
    private val _searchQuery =
        MutableStateFlow("")

    val searchQuery =
        _searchQuery.asStateFlow()
    private val osrmApi = RetrofitProvider.osrmApi

    fun setLocation(
        type: ExpressLocationType,
        address: String,
        lat: Double,
        lng: Double
    ) {
        _uiState.update { state ->
            when (type) {
                ExpressLocationType.PICKUP -> {
                    state.copy(
                        pickupAddress = address,
                        pickupLat = lat,
                        pickupLng = lng,
                        errorMessage = null
                    )
                }

                ExpressLocationType.DROPOFF -> {
                    state.copy(
                        dropoffAddress = address,
                        dropoffLat = lat,
                        dropoffLng = lng,
                        errorMessage = null
                    )
                }
            }
        }

        calculateRouteIfReady()
    }

    fun onPackageDetailChange(value: String) {
        _uiState.update {
            it.copy(
                packageDetail = value,
                errorMessage = null
            )
        }
    }

    fun toggleCod() {
        _uiState.update {
            it.copy(codEnabled = !it.codEnabled)
        }
    }

    fun toggleHandDelivery() {
        _uiState.update {
            it.copy(handDelivery = !it.handDelivery)
        }
    }

    fun toggleBigPackage() {
        _uiState.update {
            it.copy(bigPackage = !it.bigPackage)
        }
    }

    private fun calculateRouteIfReady() {
        val state = _uiState.value

        val hasPickup =
            state.pickupLat != 0.0 && state.pickupLng != 0.0

        val hasDropoff =
            state.dropoffLat != 0.0 && state.dropoffLng != 0.0

        if (!hasPickup || !hasDropoff) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val coordinates =
                    "${state.pickupLng},${state.pickupLat};${state.dropoffLng},${state.dropoffLat}"

                val response =
                    osrmApi.getRoute(coordinates)

                if (response.routes.isNotEmpty()) {
                    val route = response.routes.first()

                    val distanceKm =
                        route.distance / 1000.0

                    val durationMinute =
                        route.duration / 60.0

                    val basePrice =
                        calculateDeliveryPrice(distanceKm)

                    val routePoints =
                        PolyUtil.decode(route.geometry)

                    _uiState.update {
                        it.copy(
                            routePoints = routePoints,
                            distanceKm = distanceKm,
                            durationMinute = durationMinute,
                            basePrice = basePrice,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không tìm thấy lộ trình giao hàng"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Calculate express route failed", e)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi tính tuyến đường giao hàng"
                    )
                }
            }
        }
    }
    fun onSearchQueryChange(
        value: String
    ) {
        _searchQuery.value = value
    }
    private fun calculateDeliveryPrice(
        distanceKm: Double
    ): Double {
        val firstTwoKmPrice = 16000.0

        if (distanceKm <= 2.0) {
            return firstTwoKmPrice
        }

        val extraKm =
            ceil(distanceKm - 2.0)

        return firstTwoKmPrice + extraKm * 5000.0
    }

    fun validateBeforeConfirm(): Boolean {
        val state = _uiState.value

        if (!state.canCheckOrder) {
            _uiState.update {
                it.copy(
                    errorMessage = "Vui lòng nhập đủ địa chỉ và chi tiết món hàng"
                )
            }
            return false
        }

        if (state.basePrice <= 0.0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Chưa tính được giá giao hàng"
                )
            }
            return false
        }

        return true
    }
    fun updatePickupAddress(
        value: String
    ) {
        _uiState.update {
            it.copy(
                pickupAddress = value
            )
        }
    }

    fun updateDropoffAddress(
        value: String
    ) {
        _uiState.update {
            it.copy(
                dropoffAddress = value
            )
        }
    }
}