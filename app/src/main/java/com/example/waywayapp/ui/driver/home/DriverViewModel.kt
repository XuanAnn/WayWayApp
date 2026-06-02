package com.example.waywayapp.ui.driver.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.Ride
import com.example.waywayapp.data.remote.api.OsrmApi
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.data.repository.RideRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ViewModel điều phối màn hình tài xế: online, nhận chuyến, dẫn đường, ví và thu nhập.
class DriverViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val locationRepository: DriverLocationRepository = DriverLocationRepository(),
    private val rideRepository: RideRepository = RideRepository()
) : ViewModel() {
    // Lưu trạng thái màn hình driver để Compose tự render lại theo realtime data.
    private val _uiState = MutableStateFlow(DriverState())
    val uiState: StateFlow<DriverState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Job gửi vị trí tài xế lên Firestore định kỳ.
    private var locationJob: Job? = null
    // Job lắng nghe danh sách chuyến đang chờ tài xế nhận.
    private var openRideJob: Job? = null
    // Job lắng nghe chuyến tài xế đã nhận để cập nhật trạng thái.
    private var activeRideJob: Job? = null
    // Job lắng nghe các chuyến đã hoàn thành để tính ví/thu nhập.
    private var walletJob: Job? = null
    // Lưu các chuyến đã từ chối để không hiện lại ngay.
    private val rejectedRideIds = mutableSetOf<String>()
    // Hồ sơ driver lấy từ Firestore drivers/{uid}.
    private var driverProfile: AdminDriver? = null
    private var lastRouteAt: Long = 0L
    private var lastRouteStart: LatLng? = null
    private var lastRouteDestination: LatLng? = null

    // UID tài xế hiện tại lấy từ Firebase Auth.
    private val driverId: String?
        get() = authRepository.currentUser?.uid

    // OSRM dùng để vẽ tuyến đường tới điểm đón hoặc điểm trả.
    private val osrmApi: OsrmApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmApi::class.java)
    }

    init {
        loadDriverProfile()
    }

    // Khởi tạo client vị trí và bắt đầu gửi vị trí tài xế.
    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        refreshCurrentLocation()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    // Lấy vị trí GPS hiện tại để camera và route bắt đầu đúng chỗ.
    fun refreshCurrentLocation() {
        if (!::fusedLocationClient.isInitialized) return

        viewModelScope.launch {
            runCatching {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
            }.getOrNull()?.let { location ->
                _uiState.update {
                    it.copy(
                        currentLatLng = LatLng(location.latitude, location.longitude),
                        error = null
                    )
                }
            } ?: run {
                _uiState.update {
                    it.copy(error = "Không lấy được vị trí hiện tại")
                }
            }
        }
    }

    // Đọc hồ sơ tài xế từ Firestore để kiểm tra active và loại xe.
    private fun loadDriverProfile() {
        viewModelScope.launch {
            val id = driverId ?: return@launch
            val profile = rideRepository.getDriver(id)
            driverProfile = profile
            if (profile == null || !profile.isActive) {
                _uiState.update {
                    it.copy(
                        status = DriverStatus.OFFLINE,
                        error = "Hồ sơ tài xế chưa hoạt động. Vui lòng liên hệ admin."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    driverName = profile.name,
                    driverPhone = profile.phone,
                    driverPlate = profile.plateNumber,
                    error = null
                )
            }
            observeWallet(id)
        }
    }

    // Theo dõi chuyến đã hoàn thành để tính tổng số dư ví tài xế.
    private fun observeWallet(
        driverId: String
    ) {
        walletJob?.cancel()
        walletJob = viewModelScope.launch {
            rideRepository.observeCompletedRides(driverId)
                .collectLatest { rides ->
                    _uiState.update {
                        it.copy(
                            walletBalance = rides.sumOf { ride -> ride.price },
                            walletCompletedTrips = rides.size,
                            walletRides = rides
                        )
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    // Cứ vài giây lấy lastLocation và publish lên driver_locations cho user theo dõi.
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
                        driverId?.let { id ->
                            runCatching {
                                locationRepository.publishDriverLocation(
                                    driverId = id,
                                    activeRideId = _uiState.value.currentRideId,
                                    location = location
                                )
                            }
                        }
                        refreshRouteForCurrentStatus()
                    }
                }
                delay(5000)
            }
        }
    }

    // Bật/tắt nhận chuyến, đồng thời cập nhật trạng thái available trên Firestore.
    fun toggleOnlineStatus() {
        val profile = driverProfile
        if (profile == null || !profile.isActive) {
            loadDriverProfile()
            _uiState.update {
                it.copy(error = "Hồ sơ tài xế chưa hoạt động. Không thể bật nhận chuyến.")
            }
            return
        }

        val newStatus =
            if (_uiState.value.status == DriverStatus.OFFLINE) DriverStatus.ONLINE
            else DriverStatus.OFFLINE

        _uiState.update {
            it.copy(status = newStatus, error = null)
        }

        val isOnline = newStatus == DriverStatus.ONLINE
        viewModelScope.launch {
            driverId?.let { id ->
                runCatching {
                    locationRepository.setDriverAvailability(
                        driverId = id,
                        isOnline = isOnline,
                        isAvailable = isOnline
                    )
                }
            }

            if (isOnline) {
                observeOpenRides()
            } else {
                stopRideListeners()
                clearPendingRide()
                resetRouteTracking()
            }
        }
    }

    // Lắng nghe các chuyến searching phù hợp loại xe để hiện card nhận chuyến.
    private fun observeOpenRides() {
        openRideJob?.cancel()
        openRideJob = viewModelScope.launch {
            rideRepository.observeOpenRides(
                serviceType = driverProfile?.vehicleType?.ifBlank { "bike" } ?: "bike",
                rejectedRideIds = rejectedRideIds
            ).collectLatest { rides ->
                if (_uiState.value.status != DriverStatus.ONLINE) return@collectLatest
                val ride = rides.firstOrNull()
                if (ride == null) {
                    clearPendingRide()
                } else {
                    showRide(ride, DriverStatus.ONLINE)
                }
            }
        }
    }

    // Tài xế nhận chuyến bằng transaction để tránh nhiều driver nhận cùng một cuốc.
    fun acceptTrip() {
        val rideId = _uiState.value.currentRideId ?: return
        val driver = driverProfile ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                rideRepository.acceptRide(rideId, driver)
            }.onSuccess {
                openRideJob?.cancel()
                observeActiveRide(rideId)
                _uiState.update {
                    it.copy(status = DriverStatus.ON_THE_WAY_TO_PICKUP, isLoading = false)
                }
                refreshRouteForCurrentStatus(force = true)
            }.onFailure { throwable ->
                rejectedRideIds.add(rideId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không nhận được chuyến"
                    )
                }
                clearPendingRide()
                observeOpenRides()
            }
        }
    }

    // Theo dõi realtime ride đã nhận để UI driver đổi theo accepted/arrived/in_progress.
    private fun observeActiveRide(rideId: String) {
        activeRideJob?.cancel()
        activeRideJob = viewModelScope.launch {
            rideRepository.observeRide(rideId)
                .collectLatest { ride ->
                    if (ride == null) return@collectLatest
                    val status = when (ride.status) {
                        "accepted" -> DriverStatus.ON_THE_WAY_TO_PICKUP
                        "arrived" -> DriverStatus.ARRIVED_AT_PICKUP
                        "in_progress" -> DriverStatus.ON_TRIP
                        "completed", "cancelled" -> DriverStatus.ONLINE
                        else -> _uiState.value.status
                    }

                    showRide(ride, status)
                    refreshRouteForCurrentStatus(force = true)

                    if (ride.status == "completed" || ride.status == "cancelled") {
                        finishLocalTrip(ride.status == "completed")
                        observeOpenRides()
                    }
                }
        }
    }

    // Chuyển ride sang trạng thái đã tới điểm đón trên Firestore.
    fun arrivedAtPickup() {
        val rideId = _uiState.value.currentRideId ?: return
        val id = driverId ?: return
        viewModelScope.launch {
            runCatching {
                rideRepository.updateRideStatus(rideId, id, "arrived")
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        status = DriverStatus.ARRIVED_AT_PICKUP,
                        polylinePoints = emptyList(),
                        navigationTargetLatLng = null,
                        navigationTitle = "Chờ bắt đầu chuyến",
                        routeDistance = "",
                        routeDuration = "",
                        isRouting = false
                    )
                }
                resetRouteTracking()
            }
        }
    }

    // Bắt đầu hành trình và đổi route sang điểm trả.
    fun startTrip() {
        val rideId = _uiState.value.currentRideId ?: return
        val id = driverId ?: return
        viewModelScope.launch {
            runCatching {
                rideRepository.updateRideStatus(rideId, id, "in_progress")
            }.onSuccess {
                _uiState.update { it.copy(status = DriverStatus.ON_TRIP) }
                refreshRouteForCurrentStatus(force = true)
            }
        }
    }

    // Hoàn thành chuyến, Firestore sẽ lưu completed để cộng vào ví tài xế.
    fun completeTrip() {
        val rideId = _uiState.value.currentRideId ?: return
        val id = driverId ?: return
        viewModelScope.launch {
            runCatching {
                rideRepository.updateRideStatus(rideId, id, "completed")
            }.onSuccess {
                finishLocalTrip(completed = true)
                observeOpenRides()
            }
        }
    }

    // Từ chối chuyến ở local để driver tiếp tục xem chuyến khác.
    fun rejectTrip() {
        _uiState.value.currentRideId?.let { rideId ->
            rejectedRideIds.add(rideId)
        }
        clearPendingRide()
        observeOpenRides()
    }

    // Chọn đích dẫn đường theo trạng thái hiện tại rồi gọi OSRM vẽ route.
    private fun refreshRouteForCurrentStatus(force: Boolean = false) {
        val state = _uiState.value
        val start = state.currentLatLng ?: return
        val destination = when (state.status) {
            DriverStatus.ON_THE_WAY_TO_PICKUP -> state.pickupLatLng
            DriverStatus.ON_TRIP -> state.dropoffLatLng
            else -> null
        } ?: return

        val now = System.currentTimeMillis()
        if (!force && !shouldRefreshRoute(start, destination, now)) {
            return
        }

        calculateRoute(
            start = start,
            end = destination,
            title = when (state.status) {
                DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đang dẫn tới điểm đón"
                DriverStatus.ON_TRIP -> "Đang dẫn tới điểm trả"
                else -> ""
            }
        )
    }

    // Giới hạn tần suất vẽ route để tránh gọi OSRM quá nhiều.
    private fun shouldRefreshRoute(
        start: LatLng,
        destination: LatLng,
        now: Long
    ): Boolean {
        val previousStart = lastRouteStart
        val previousDestination = lastRouteDestination
        if (previousStart == null || previousDestination == null) return true
        if (distanceMeters(previousDestination, destination) > 10f) return true
        if (distanceMeters(previousStart, start) > 40f) return true
        return now - lastRouteAt > 20_000L
    }

    // Gọi OSRM để lấy polyline và ETA hiển thị trên màn driver.
    private fun calculateRoute(
        start: LatLng,
        end: LatLng,
        title: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRouting = true,
                    navigationTargetLatLng = end,
                    navigationTitle = title
                )
            }
            runCatching {
                val coordinates =
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"
                val response = osrmApi.getRoute(coordinates)
                if (response.routes.isNotEmpty()) {
                    val route = response.routes[0]
                    val points = PolyUtil.decode(route.geometry)
                    lastRouteAt = System.currentTimeMillis()
                    lastRouteStart = start
                    lastRouteDestination = end
                    _uiState.update {
                        it.copy(
                            polylinePoints = points,
                            navigationTargetLatLng = end,
                            navigationTitle = title,
                            routeDistance = formatDistance(route.distance),
                            routeDuration = formatDuration(route.duration),
                            isRouting = false,
                            error = null
                        )
                    }
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isRouting = false,
                        navigationTargetLatLng = end,
                        navigationTitle = title,
                        error = "Không vẽ được tuyến đường, vui lòng thử lại."
                    )
                }
            }
        }
    }

    // Đưa dữ liệu Ride từ Firestore vào DriverState để card chuyến hiển thị.
    private fun showRide(
        ride: Ride,
        status: DriverStatus
    ) {
        _uiState.update {
            it.copy(
                status = status,
                currentRideId = ride.id,
                pickupAddress = ride.pickupAddress,
                dropoffAddress = ride.dropoffAddress,
                passengerName = ride.passengerName.ifBlank { "Khách hàng" },
                passengerPhone = ride.passengerPhone,
                tripPrice = ride.price,
                pickupLatLng = LatLng(ride.pickupLat, ride.pickupLng),
                dropoffLatLng = LatLng(ride.dropoffLat, ride.dropoffLng),
                polylinePoints = if (status == DriverStatus.ARRIVED_AT_PICKUP) emptyList() else it.polylinePoints,
                navigationTargetLatLng = when (status) {
                    DriverStatus.ON_THE_WAY_TO_PICKUP -> LatLng(ride.pickupLat, ride.pickupLng)
                    DriverStatus.ON_TRIP -> LatLng(ride.dropoffLat, ride.dropoffLng)
                    DriverStatus.ARRIVED_AT_PICKUP -> null
                    else -> it.navigationTargetLatLng
                },
                navigationTitle = when (status) {
                    DriverStatus.ON_THE_WAY_TO_PICKUP -> "Đang dẫn tới điểm đón"
                    DriverStatus.ON_TRIP -> "Đang dẫn tới điểm trả"
                    DriverStatus.ARRIVED_AT_PICKUP -> "Chờ bắt đầu chuyến"
                    else -> it.navigationTitle
                },
                routeDistance = if (status == DriverStatus.ARRIVED_AT_PICKUP) "" else it.routeDistance,
                routeDuration = if (status == DriverStatus.ARRIVED_AT_PICKUP) "" else it.routeDuration
            )
        }
    }

    // Xoá thông tin chuyến đang chờ khi không còn chuyến phù hợp.
    private fun clearPendingRide() {
        _uiState.update {
            it.copy(
                currentRideId = null,
                pickupLatLng = null,
                dropoffLatLng = null,
                pickupAddress = "",
                dropoffAddress = "",
                passengerName = "",
                passengerPhone = "",
                tripPrice = 0.0,
                polylinePoints = emptyList(),
                navigationTargetLatLng = null,
                navigationTitle = "",
                routeDistance = "",
                routeDuration = "",
                isRouting = false
            )
        }
        resetRouteTracking()
    }

    // Reset state sau khi chuyến kết thúc và cộng thu nhập local nếu completed.
    private fun finishLocalTrip(
        completed: Boolean
    ) {
        val fare = _uiState.value.tripPrice
        _uiState.update { state ->
            state.copy(
                status = DriverStatus.ONLINE,
                currentEarnings = if (completed) state.currentEarnings + fare else state.currentEarnings,
                currentTrips = if (completed) state.currentTrips + 1 else state.currentTrips,
                pickupLatLng = null,
                dropoffLatLng = null,
                currentRideId = null,
                pickupAddress = "",
                dropoffAddress = "",
                passengerName = "",
                passengerPhone = "",
                tripPrice = 0.0,
                polylinePoints = emptyList(),
                navigationTargetLatLng = null,
                navigationTitle = "",
                routeDistance = "",
                routeDuration = "",
                isRouting = false
            )
        }
        resetRouteTracking()
    }

    // Xoá cache route để lần sau bắt buộc tính lại đường đi.
    private fun resetRouteTracking() {
        lastRouteAt = 0L
        lastRouteStart = null
        lastRouteDestination = null
    }

    // Tính khoảng cách giữa hai LatLng để quyết định có cần refresh route không.
    private fun distanceMeters(
        start: LatLng,
        end: LatLng
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            result
        )
        return result[0]
    }

    // Format khoảng cách OSRM thành m hoặc km cho UI.
    private fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            String.format("%.1f km", distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }
    }

    // Format thời gian OSRM thành phút cho UI.
    private fun formatDuration(durationSeconds: Double): String {
        val minutes = (durationSeconds / 60.0).toInt().coerceAtLeast(1)
        return "$minutes phút"
    }

    // Dừng listener open/active ride khi tài xế offline hoặc ViewModel bị huỷ.
    private fun stopRideListeners() {
        openRideJob?.cancel()
        activeRideJob?.cancel()
        openRideJob = null
        activeRideJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRideListeners()
        walletJob?.cancel()
        locationJob?.cancel()
    }
}
