package com.example.waywayapp.ui.driver.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.remote.api.OsrmApi
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DriverViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val locationRepository: DriverLocationRepository = DriverLocationRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverState())
    val uiState: StateFlow<DriverState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationJob: Job? = null

    private val osrmApi: OsrmApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmApi::class.java)
    }

    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationJob?.isActive == true) {
            return
        }

        locationJob = viewModelScope.launch {
            while (true) {
                runCatching {
                    fusedLocationClient.lastLocation.await()
                }.getOrNull()?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiState.update { state ->
                        state.copy(currentLatLng = latLng)
                    }

                    if (_uiState.value.status != DriverStatus.OFFLINE) {
                        val driverId = authRepository.currentUser?.uid ?: "demo-driver"
                        runCatching {
                            locationRepository.publishDriverLocation(
                                driverId = driverId,
                                activeRideId = _uiState.value.currentRideId,
                                location = location
                            )
                        }
                    }
                }
                delay(5000)
            }
        }
    }

    fun toggleOnlineStatus() {
        _uiState.update {
            val newStatus =
                if (it.status == DriverStatus.OFFLINE) DriverStatus.ONLINE
                else DriverStatus.OFFLINE
            it.copy(status = newStatus)
        }

        val isOnline = _uiState.value.status == DriverStatus.ONLINE
        viewModelScope.launch {
            val driverId = authRepository.currentUser?.uid ?: "demo-driver"
            runCatching {
                locationRepository.setDriverAvailability(
                    driverId = driverId,
                    isOnline = isOnline,
                    isAvailable = isOnline
                )
            }

            if (isOnline) {
                delay(3000)
                simulateIncomingTrip()
            }
        }
    }

    private fun simulateIncomingTrip() {
        _uiState.update {
            it.copy(
                currentRideId = "demo-ride",
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
        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return
        calculateRoute(pickup, dropoff)
    }

    fun startTrip() {
        _uiState.update { it.copy(status = DriverStatus.ON_TRIP) }
    }

    private fun calculateRoute(start: LatLng, end: LatLng) {
        viewModelScope.launch {
            runCatching {
                val coordinates =
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"
                val response = osrmApi.getRoute(coordinates)
                if (response.routes.isNotEmpty()) {
                    val points = PolyUtil.decode(response.routes[0].geometry)
                    _uiState.update { it.copy(polylinePoints = points) }
                }
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
                currentRideId = null,
                polylinePoints = emptyList()
            )
        }
    }

    fun rejectTrip() {
        _uiState.update {
            it.copy(
                pickupLatLng = null,
                dropoffLatLng = null,
                currentRideId = null,
                pickupAddress = "",
                dropoffAddress = ""
            )
        }
    }
}
