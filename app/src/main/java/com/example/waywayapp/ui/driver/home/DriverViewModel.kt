package com.example.waywayapp.ui.driver.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.ui.user.booking.bike.OsrmService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

class DriverViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DriverState())
    val uiState: StateFlow<DriverState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val osrmService: OsrmService by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmService::class.java)
    }

    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        viewModelScope.launch {
            while (true) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _uiState.update { state -> state.copy(currentLatLng = latLng) }
                    }
                }
                delay(5000)
            }
        }
    }

    fun toggleOnlineStatus() {
        _uiState.update { 
            val newStatus = if (it.status == DriverStatus.OFFLINE) DriverStatus.ONLINE else DriverStatus.OFFLINE
            it.copy(status = newStatus)
        }
        
        if (_uiState.value.status == DriverStatus.ONLINE) {
            viewModelScope.launch {
                delay(3000)
                simulateIncomingTrip()
            }
        }
    }

    private fun simulateIncomingTrip() {
        _uiState.update { 
            it.copy(
                pickupAddress = "123 Đường Lê Lợi, Quận 1",
                dropoffAddress = "Landmark 81, Bình Thạnh",
                passengerName = "Trần Thị B",
                passengerPhone = "0987654321",
                tripPrice = 45000.0,
                pickupLatLng = LatLng(10.7769, 106.7009),
                dropoffLatLng = LatLng(10.7947, 106.7218)
            )
        }
    }

    fun acceptTrip() {
        _uiState.update { it.copy(status = DriverStatus.ON_THE_WAY_TO_PICKUP) }
        calculateRouteToPickup()
    }

    private fun calculateRouteToPickup() {
        val current = _uiState.value.currentLatLng ?: return
        val pickup = _uiState.value.pickupLatLng ?: return
        calculateRoute(current, pickup)
    }

    fun arrivedAtPickup() {
        // Driver reached pickup point, now calculate route to dropoff
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return
        calculateRoute(pickup, dropoff)
    }

    fun startTrip() {
        _uiState.update { it.copy(status = DriverStatus.ON_TRIP) }
    }

    private fun calculateRoute(start: LatLng, end: LatLng) {
        viewModelScope.launch {
            try {
                val coordinates = "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"
                val response = osrmService.getRoute(coordinates)
                if (response.routes.isNotEmpty()) {
                    val points = PolyUtil.decode(response.routes[0].geometry)
                    _uiState.update { it.copy(polylinePoints = points) }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun completeTrip() {
        _uiState.update { state ->
            state.copy(
                status = DriverStatus.ONLINE,
                currentEarnings = state.currentEarnings + state.tripPrice,
                currentTrips = state.currentTrips + 1,
                pickupLatLng = null,
                dropoffLatLng = null,
                polylinePoints = emptyList()
            )
        }
    }

    fun rejectTrip() {
        _uiState.update { 
            it.copy(
                pickupLatLng = null,
                dropoffLatLng = null,
                pickupAddress = "",
                dropoffAddress = ""
            )
        }
    }
}
