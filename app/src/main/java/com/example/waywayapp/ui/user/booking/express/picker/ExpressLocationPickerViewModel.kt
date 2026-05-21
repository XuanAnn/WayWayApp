package com.example.waywayapp.ui.user.booking.express.picker

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
import com.example.waywayapp.ui.user.booking.express.ExpressSharedViewModel
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ExpressLocationPickerViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(ExpressLocationPickerState())

    val uiState =
        _uiState.asStateFlow()
    private val TAG = "ExpressPickerVM"
    private var geocoder: Geocoder? = null

    private val geocodingApi =
        RetrofitProvider.geocodingApi

    init {
        viewModelScope.launch {
            uiState
                .debounce(500)
                .filter { it.searchQuery.length > 2 }
                .distinctUntilChanged()
                .collect { state ->
                    performAutocomplete(state.searchQuery)
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun initLocation(context: Context) {
        geocoder = Geocoder(context, Locale("vi", "VN"))

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        _uiState.update {
            it.copy(isLoading = true)
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->

            val latLng =
                if (location != null) {
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                } else {
                    LatLng(16.0718, 108.2208)
                }

            _uiState.update {
                it.copy(
                    currentLatLng = latLng,
                    selectedLatLng = latLng,
                    isLoading = false
                )
            }

            updateSelectedLocation(latLng)

        }.addOnFailureListener {
            val fallback =
                LatLng(16.0718, 108.2208)

            _uiState.update {
                it.copy(
                    currentLatLng = fallback,
                    selectedLatLng = fallback,
                    isLoading = false,
                    errorMessage = "Không lấy được vị trí hiện tại"
                )
            }

            updateSelectedLocation(fallback)
        }
    }

    fun updateSelectedLocation(latLng: LatLng) {
        _uiState.update {
            it.copy(
                selectedLatLng = latLng,
                selectedAddress = "Đang lấy địa chỉ..."
            )
        }

        getAddressFromLatLng(latLng) { address ->
            _uiState.update {
                it.copy(selectedAddress = address)
            }
        }
    }

    fun confirmLocation(
        type: ExpressLocationType,
        onDone: () -> Unit
    ) {
        val state = _uiState.value
        val latLng = state.selectedLatLng ?: return

        ExpressSharedViewModel.viewModel.setLocation(
            type = type,
            address = state.selectedAddress,
            lat = latLng.latitude,
            lng = latLng.longitude
        )

        onDone()
    }
    fun onSearchQueryChange(value: String) {
        _uiState.update {
            it.copy(searchQuery = value)
        }
    }

    private suspend fun performAutocomplete(query: String) {
        try {
            val results =
                geocodingApi.searchAddress(query = query)

            _uiState.update {
                it.copy(searchResults = results)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Autocomplete error: ${e.message}")
        }
    }

    fun selectSearchResult(result: GeocodingResponseDto) {
        val latLng = LatLng(
            result.lat.toDouble(),
            result.lon.toDouble()
        )

        _uiState.update {
            it.copy(
                selectedLatLng = latLng,
                selectedAddress = result.display_name,
                searchQuery = result.display_name,
                searchResults = emptyList()
            )
        }
    }

    fun clearSearchResults() {
        _uiState.update {
            it.copy(searchResults = emptyList())
        }
    }
    private fun getAddressFromLatLng(
        latLng: LatLng,
        onResult: (String) -> Unit
    ) {
        val currentGeocoder = geocoder ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentGeocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                ) { addresses ->
                    val address =
                        addresses.firstOrNull()

                    onResult(
                        address?.let {
                            formatAddress(it)
                        } ?: "Vị trí không xác định"
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses =
                    currentGeocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )

                val address =
                    addresses?.firstOrNull()

                onResult(
                    address?.let {
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