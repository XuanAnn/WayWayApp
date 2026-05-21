package com.example.waywayapp.ui.user.booking.bike

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.model.Promo
import com.example.waywayapp.data.repository.PromoRepository
import com.example.waywayapp.ui.user.booking.bike.model.BookingStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.floor
import com.example.waywayapp.data.remote.api.GeocodingApi
import com.example.waywayapp.data.remote.api.OsrmApi
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.ui.user.booking.bike.model.BikeLocationType

class BikeViewModel(

) : ViewModel(

) {
    // Tag dùng để filter trong Logcat
    private val TAG = "WayWay_BikeVM"

    private val _uiState = MutableStateFlow(BikeState())
    val uiState: StateFlow<BikeState> = _uiState.asStateFlow()
    private val promoRepository = PromoRepository()

    private val _availablePromos = MutableStateFlow<List<Promo>>(emptyList())
    val availablePromos = _availablePromos.asStateFlow()
    // 1. Khai báo OkHttpClient để thêm Header User-Agent
    private val geocodingApi =
        RetrofitProvider.geocodingApi

    private val osrmApi =
        RetrofitProvider.osrmApi


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    // Thêm vào BikeViewModel
    private val _searchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<GeocodingResponseDto>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResponseDto>> = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Chờ 500ms sau khi người dùng ngừng gõ mới gọi API
                .filter { it.length > 2 } // Chỉ tìm kiếm khi nhập trên 2 ký tự
                .distinctUntilChanged() // Chỉ gọi nếu nội dung thay đổi so với lần trước
                .collect { query ->
                    performAutocomplete(query)
                }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _uiState.update { it.copy(dropoffAddress = newQuery) }
    }
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
    private suspend fun performAutocomplete(query: String) {
        try {
            val results = geocodingApi.searchAddress(query = query)
            _searchResults.value = results
        } catch (e: Exception) {
            Log.e(TAG, "Autocomplete Error: ${e.message}")
        }
    }
    fun initLocationClient(context: Context) {
        Log.d(TAG, "Initializing Location Client")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        geocoder = Geocoder(context, Locale("vi", "VN"))
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        Log.d(TAG, "Requesting current location...")

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.i(TAG, "Current location found: $latLng")

                    _uiState.update { state ->
                        state.copy(currentLatLng = latLng, pickupLatLng = latLng)
                    }
                    getAddressFromLatLng(latLng) { address ->
                        _uiState.update { it.copy(pickupAddress = address) }
                    }
                } else {
                    Log.w(TAG, "Current location is null")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get current location: ${e.message}", e)
                _uiState.update { it.copy(error = "Không thể lấy vị trí hiện tại") }
            }
    }

    private fun getAddressFromLatLng(latLng: LatLng, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Reverse geocoding for: $latLng")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            onResult(formatAddress(addr))
                        } else {
                            Log.w(TAG, "No address found for LatLng: $latLng")
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        onResult(formatAddress(addresses[0]))
                    } else {
                        Log.w(TAG, "No address found for LatLng: $latLng")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoder Exception: ${e.message}", e)
                onResult("Vị trí không xác định")
            }
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        Log.d(TAG, "Searching for location: $query")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = geocodingApi.searchAddress(query = query)

                if (results.isNotEmpty()) {
                    val firstResult = results[0]
                    val latLng = LatLng(firstResult.lat.toDouble(), firstResult.lon.toDouble())
                    Log.i(TAG, "Search success: ${firstResult.display_name} at $latLng")

                    _uiState.update { state ->
                        state.copy(
                            dropoffLatLng = latLng,
                            dropoffAddress = firstResult.display_name,
                            isLoading = false,
                            error = null
                        )
                    }
                    calculateRoute()
                } else {
                    Log.w(TAG, "Search returned no results for: $query")
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy địa điểm này") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding API Error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi kết nối máy chủ tìm kiếm") }
            }
        }
    }

    private fun formatAddress(addr: Address): String {
        val featureName = addr.featureName
        val street = addr.thoroughfare
        val district = addr.subAdminArea
        val city = addr.adminArea

        val combined = buildString {
            if (!featureName.isNullOrEmpty()) append("$featureName ")
            if (!street.isNullOrEmpty()) {
                if (featureName != street) append(street)
            }
            if (!district.isNullOrEmpty()) append(", $district")
            if (!city.isNullOrEmpty()) append(", $city")
        }.trim().trimEnd(',')

        return combined.ifBlank { addr.getAddressLine(0) ?: "Vị trí không xác định" }
    }

    fun onPickupAddressChange(address: String) {
        _uiState.update { it.copy(pickupAddress = address) }
    }

    fun onDropoffAddressChange(address: String) {
        _uiState.update { it.copy(dropoffAddress = address) }
    }

    fun setDropoffLocation(latLng: LatLng, defaultText: String) {
        Log.d(TAG, "Setting dropoff location to: $latLng")
        _uiState.update { it.copy(dropoffLatLng = latLng, dropoffAddress = "Đang lấy địa chỉ...") }
        getAddressFromLatLng(latLng) { address ->
            _uiState.update { it.copy(dropoffAddress = address) }
            calculateRoute()
        }
    }

    fun calculateRoute() {
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return
        val promo = _uiState.value.promo ?: 1.0
        Log.d(TAG, "Calculating route from $pickup to $dropoff")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val coordinates = "${pickup.longitude},${pickup.latitude};${dropoff.longitude},${dropoff.latitude}"
                val response = osrmApi.getRoute(coordinates)

                if (response.routes.isNotEmpty()) {
                    val route = response.routes[0]
                    val points = PolyUtil.decode(route.geometry)
                    Log.i(TAG, "Route found: ${route.distance}m, ${route.duration}s")

                    _uiState.update { state ->
                        val basePrice = (floor(route.distance / 1000) * 5000 + 12000)
                        state.copy(
                            polylinePoints = points,
                            distance = String.format("%.1f km", route.distance / 1000),
                            duration = String.format("%.0f phút", route.duration / 60),
                            price = basePrice,
                            finalPrice = basePrice,
                            isLoading = false
                        )
                    }
                } else {
                    Log.w(TAG, "OSRM returned no routes")
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy lộ trình") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "OSRM Service Error: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tính toán lộ trình") }
            }
        }
    }

    fun startBooking() {
        Log.i(TAG, "Booking process started")
        viewModelScope.launch {
            _uiState.update { it.copy(status = BookingStatus.FINDING) }
            delay(3000)
            Log.d(TAG, "Driver found, entering ON_TRIP")
            _uiState.update { it.copy(status = BookingStatus.ON_TRIP) }
        }
    }

    fun completeTrip() {
        Log.i(TAG, "Trip completed")
        _uiState.update { it.copy(status = BookingStatus.COMPLETED) }
    }

    fun resetBooking() {
        Log.d(TAG, "Resetting booking screen")
        _uiState.update {
            BikeState(
                currentLatLng = it.currentLatLng,
                pickupLatLng = it.currentLatLng,
                pickupAddress = it.pickupAddress
            )
        }
    }
    fun setPickupLocation(
        latLng: LatLng,
        address: String
    ) {
        _uiState.update {
            it.copy(
                pickupLatLng = latLng,
                pickupAddress = address,
                error = null
            )
        }

        calculateRoute()
    }

    fun setBikeLocationFromMap(
        type: BikeLocationType,
        latLng: LatLng
    ) {
        when (type) {
            BikeLocationType.PICKUP -> {
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

            BikeLocationType.DROPOFF -> {
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
    fun cancelBooking() {
        Log.i(TAG, "Booking canceled")
        _uiState.update { it.copy(status = BookingStatus.IDLE) }
    }
    fun applyPromo(promo: Promo) {
        val currentPrice = uiState.value.price
        if (promo.minPrice != null && currentPrice < promo.minPrice) {
            _uiState.update {
                it.copy(error = "Đơn tối thiểu ${promo.minPrice}đ mới áp dụng được mã này")
            }
            return
        }
        val discountAmount = promoRepository.calculateDiscount(currentPrice, promo)

        _uiState.update { it.copy(
            promoCode = promo.code,
            discount = discountAmount,
            finalPrice = currentPrice - discountAmount
        )}
    }
    // Trong BikeViewModel
    fun loadPromos() {
        viewModelScope.launch {
            try {
                // Lấy dữ liệu từ Repository
                val list = promoRepository.getAvailablePromos()
                _availablePromos.value = list
                Log.d(TAG, "Promos loaded: ${list.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load promos", e)
            }
        }
    }
}