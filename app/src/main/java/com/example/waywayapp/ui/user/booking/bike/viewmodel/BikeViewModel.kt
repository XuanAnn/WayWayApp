package com.example.waywayapp.ui.user.booking.bike.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.bike.OsrmService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.floor

class BikeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BikeState())
    val uiState: StateFlow<BikeState> = _uiState.asStateFlow()

    private val osrmService: OsrmService by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmService::class.java)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        geocoder = Geocoder(context, Locale("vi", "VN"))
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    _uiState.update { state ->
                        state.copy(currentLatLng = latLng, pickupLatLng = latLng)
                    }
                    getAddressFromLatLng(latLng) { address ->
                        _uiState.update { it.copy(pickupAddress = address) }
                    }
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(error = "Không thể lấy vị trí hiện tại") }
            }
    }
    private fun getAddressFromLatLng(latLng: LatLng, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            onResult(formatAddress(addr))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        onResult(formatAddress(addresses[0]))
                    }
                }
            } catch (e: Exception) {
                onResult("Vị trí không xác định")
            }
        }
    }

    // Hàm bổ trợ để định dạng địa chỉ chuyên nghiệp hơn
    private fun formatAddress(addr: Address): String {
        val featureName = addr.featureName // Số nhà
        val street = addr.thoroughfare // Tên đường
        val district = addr.subAdminArea // Quận
        val city = addr.adminArea // Thành phố

        val combined = buildString {
            if (!featureName.isNullOrEmpty()) append("$featureName ")
            if (!street.isNullOrEmpty()) {
                if (featureName != street) append(street) // Tránh lặp nếu số nhà trùng tên đường
            }
            if (!district.isNullOrEmpty()) append(", $district")
            if (!city.isNullOrEmpty()) append(", $city")
        }.trim().trimEnd(',')

        // Nếu build chuỗi thất bại hoặc quá ngắn, dùng address line mặc định của Google
        return combined.ifBlank { addr.getAddressLine(0) ?: "Vị trí không xác định" }
    }

    fun onPickupAddressChange(address: String) {
        _uiState.update { it.copy(pickupAddress = address) }
    }

    fun onDropoffAddressChange(address: String) {
        _uiState.update { it.copy(dropoffAddress = address) }
    }

    fun setDropoffLocation(latLng: LatLng, defaultText: String) {
        _uiState.update { it.copy(dropoffLatLng = latLng, dropoffAddress = "Đang lấy địa chỉ...") }
        getAddressFromLatLng(latLng) { address ->
            _uiState.update { it.copy(dropoffAddress = address) }
            calculateRoute()
        }
    }

    fun calculateRoute() {
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val coordinates = "${pickup.longitude},${pickup.latitude};${dropoff.longitude},${dropoff.latitude}"
                val response = osrmService.getRoute(coordinates)

                if (response.routes.isNotEmpty()) {
                    val route = response.routes[0]
                    val points = PolyUtil.decode(route.geometry)

                    _uiState.update { state ->
                        state.copy(
                            polylinePoints = points,
                            distance = String.format("%.1f km", route.distance / 1000),
                            duration = String.format("%.0f phút", route.duration / 60),
                            price = floor(route.distance / 1000) * 5000 + 12000,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun startBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = BookingStatus.FINDING) }
            delay(3000)
            _uiState.update { it.copy(status = BookingStatus.ON_TRIP) }
        }
    }

    fun completeTrip() {
        _uiState.update { it.copy(status = BookingStatus.COMPLETED) }
    }

    fun resetBooking() {
        _uiState.update {
            BikeState(
                currentLatLng = it.currentLatLng,
                pickupLatLng = it.currentLatLng,
                pickupAddress = it.pickupAddress
            )
        }
    }

    fun cancelBooking() {
        _uiState.update { it.copy(status = BookingStatus.IDLE) }
    }
}
