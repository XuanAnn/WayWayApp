package com.example.waywayapp.ui.user.booking.bike

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
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

    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        getCurrentLocation(context)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)

                viewModelScope.launch(Dispatchers.IO) {
                    val address = getAddressFromLatLng(context, latLng)

                    _uiState.update { state ->
                        state.copy(
                            currentLatLng = latLng,
                            pickupLatLng = latLng,
                            pickupAddress = address
                        )
                    }
                }
            }
        }
    }
    suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )

            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Không xác định"
            } else {
                "Không xác định"
            }
        } catch (e: Exception) {
            "Không xác định"
        }
    }

    fun setDropoffLocation(latLng: LatLng, address: String) {
        _uiState.update { it.copy(dropoffLatLng = latLng, dropoffAddress = address) }
        calculateRoute()
    }

    private fun calculateRoute() {
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val coordinates =
                    "${pickup.longitude},${pickup.latitude};${dropoff.longitude},${dropoff.latitude}"
                val response = osrmService.getRoute(coordinates)

                if (response.routes.isNotEmpty()) {
                    val route = response.routes[0]
                    val points = PolyUtil.decode(route.geometry)

                    _uiState.update { state ->
                        state.copy(
                            polylinePoints = points,
                            distance = String.format("%.1f km", route.distance / 1000),
                            duration = String.format("%.0f phút", route.duration / 60),
                            price = floor(route.distance / 1000) * 5000 + 12000, // Ví dụ: 12k mở cửa + 5k/km
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
