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
import com.example.waywayapp.data.repository.DriverLocationRepository
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.data.repository.MomoPaymentRepository
import com.example.waywayapp.data.repository.PromoRepository
import com.example.waywayapp.data.repository.RideRepository
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
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.Normalizer
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import com.example.waywayapp.data.remote.api.GeocodingApi
import com.example.waywayapp.data.remote.api.OsrmApi
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.ui.user.booking.bike.model.BikeLocationType

// ViewModel điều phối luồng đặt xe máy: vị trí, tuyến đường, khuyến mãi, thanh toán và theo dõi tài xế.
class BikeViewModel(

) : ViewModel(

) {
    // Tag dùng để filter trong Logcat
    private val TAG = "WayWay_BikeVM"

    // Lưu toàn bộ trạng thái UI của màn đặt xe để Compose tự cập nhật.
    private val _uiState = MutableStateFlow(BikeState())
    val uiState: StateFlow<BikeState> = _uiState.asStateFlow()
    // Repository đọc mã giảm giá từ Firestore và tính số tiền được giảm.
    private val promoRepository = PromoRepository()
    // Repository thao tác collection rides để tạo, theo dõi và huỷ chuyến.
    private val rideRepository = RideRepository()
    // Repository đọc realtime vị trí tài xế từ collection driver_locations.
    private val driverLocationRepository = DriverLocationRepository()
    // Repository lấy user hiện tại từ Firebase Authentication.
    private val authRepository = FirebaseAuthRepository()
    // Repository gọi backend Spring Boot để tạo và kiểm tra đơn MoMo.
    private val momoPaymentRepository = MomoPaymentRepository()
    // Job theo dõi realtime document ride hiện tại.
    private var rideJob: Job? = null
    // Job theo dõi realtime vị trí tài xế sau khi đã match chuyến.
    private var driverLocationJob: Job? = null
    // Job cập nhật lại polyline tài xế -> điểm đón/điểm trả mỗi 15 giây.
    private var trackingRouteJob: Job? = null
    private var lastTrackingRouteAt: Long = 0L
    // Lưu tuyến đường ban đầu pickup -> dropoff để không mất polyline khi tài xế nhận cuốc.
    private var bookingPolylinePoints: List<LatLng> = emptyList()

    // Danh sách mã giảm giá đang có hiệu lực để hiển thị cho user.
    private val _availablePromos = MutableStateFlow<List<Promo>>(emptyList())
    val availablePromos = _availablePromos.asStateFlow()
    // 1. Khai báo OkHttpClient để thêm Header User-Agent
    private val geocodingApi =
        RetrofitProvider.geocodingApi

    // API OSRM dùng để lấy polyline, quãng đường và thời gian di chuyển.
    private val osrmApi =
        RetrofitProvider.osrmApi


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    // Thêm vào BikeViewModel
    private val _searchQuery = MutableStateFlow("")
    // Kết quả autocomplete trả về từ geocoding API.
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

    // Cập nhật ô tìm kiếm điểm đến và đồng bộ vào state của form đặt xe.
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _uiState.update { it.copy(dropoffAddress = newQuery) }
    }
    // Xoá kết quả gợi ý khi user đã chọn hoặc rời ô tìm kiếm.
    fun selectServiceType(serviceType: String) {
        val normalizedType = if (serviceType == "car") "car" else "bike"
        _uiState.update { state ->
            val selectedPrice = if (normalizedType == "car") state.carPrice else state.bikePrice
            val fallbackPrice = selectedPrice.takeIf { it > 0.0 } ?: state.price
            state.copy(
                selectedServiceType = normalizedType,
                price = fallbackPrice,
                finalPrice = (fallbackPrice - state.discount).coerceAtLeast(0.0)
            )
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
    // Gọi geocoding API để lấy danh sách địa chỉ gợi ý theo từ khoá.
    private suspend fun performAutocomplete(query: String) {
        try {
            val results = searchAddressWithFallback(query)
            _searchResults.value = results
        } catch (e: Exception) {
            Log.e(TAG, "Autocomplete Error: ${e.message}")
        }
    }
    // Khởi tạo dịch vụ vị trí của Android và lấy vị trí hiện tại làm điểm đón mặc định.
    fun initLocationClient(context: Context) {
        Log.d(TAG, "Initializing Location Client")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        geocoder = Geocoder(context, Locale("vi", "VN"))
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    // Lấy GPS hiện tại của user rồi reverse geocode thành địa chỉ điểm đón.
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

    // Chuyển LatLng thành địa chỉ chữ để hiển thị trong form đặt xe.
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

    // Tìm một địa điểm theo text và đặt kết quả đầu tiên làm điểm đến.
    fun searchLocation(query: String) {
        if (query.isBlank()) return
        Log.d(TAG, "Searching for location: $query")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = searchAddressWithFallback(query)

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

    private suspend fun searchAddressWithFallback(query: String): List<GeocodingResponseDto> {
        val results = geocodingApi.searchAddress(query = query)
        if (results.isNotEmpty()) return results

        val fallbackQuery = query.withoutVietnameseDiacritics()
        if (fallbackQuery == query) return results

        return geocodingApi.searchAddress(query = fallbackQuery)
    }

    private fun String.withoutVietnameseDiacritics(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    // Rút gọn Address của Android thành chuỗi dễ đọc cho UI.
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

    // Cập nhật địa chỉ điểm đón khi user nhập thủ công.
    fun onPickupAddressChange(address: String) {
        _uiState.update { it.copy(pickupAddress = address) }
    }

    // Cập nhật địa chỉ điểm đến khi user nhập thủ công.
    fun onDropoffAddressChange(address: String) {
        _uiState.update { it.copy(dropoffAddress = address) }
    }

    // Gán điểm đến khi user chọn trên bản đồ rồi tính lại tuyến đường.
    fun setDropoffLocation(latLng: LatLng, defaultText: String) {
        Log.d(TAG, "Setting dropoff location to: $latLng")
        _uiState.update { it.copy(dropoffLatLng = latLng, dropoffAddress = "Đang lấy địa chỉ...") }
        getAddressFromLatLng(latLng) { address ->
            _uiState.update { it.copy(dropoffAddress = address) }
            calculateRoute()
        }
    }

    // Gọi OSRM để tính route pickup -> dropoff, đồng thời tính giá cước dự kiến.
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
                        val distanceKm = route.distance / 1000.0
                        val bikePrice = calculateBikePrice(distanceKm)
                        val carPrice = calculateCarPrice(distanceKm)
                        val selectedPrice = if (state.selectedServiceType == "car") carPrice else bikePrice
                        bookingPolylinePoints = points
                        state.copy(
                            polylinePoints = points,
                            distance = String.format("%.1f km", distanceKm),
                            duration = formatDuration(route.duration),
                            etaToDropoff = formatDuration(route.duration),
                            bikePrice = bikePrice,
                            carPrice = carPrice,
                            price = selectedPrice,
                            finalPrice = selectedPrice,
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

    // Tạo document chuyến xe trên Firestore sau khi user xác nhận đặt xe.
    fun startBooking(paymentMethod: String = "cash") {
        Log.i(TAG, "Booking process started")
        viewModelScope.launch {
            val pickup = _uiState.value.pickupLatLng
            val dropoff = _uiState.value.dropoffLatLng
            if (pickup == null || dropoff == null) {
                _uiState.update { it.copy(error = "Vui lòng chọn đủ điểm đón và điểm đến") }
                return@launch
            }

            _uiState.update { it.copy(status = BookingStatus.FINDING, isLoading = true, error = null) }
            runCatching {
                rideRepository.createBikeRide(
                    userId = authRepository.currentUser?.uid.orEmpty(),
                    pickup = pickup,
                    pickupAddress = _uiState.value.pickupAddress,
                    dropoff = dropoff,
                    dropoffAddress = _uiState.value.dropoffAddress,
                    price = _uiState.value.finalPrice.takeIf { price -> price > 0.0 }
                        ?: _uiState.value.price,
                    serviceType = _uiState.value.selectedServiceType,
                    paymentMethod = paymentMethod,
                    paymentStatus = if (paymentMethod == "momo_uat" || paymentMethod == "momo_gateway") "paid" else "pending",
                    paidAt = if (paymentMethod == "momo_uat" || paymentMethod == "momo_gateway") System.currentTimeMillis() else null
                )
            }.onSuccess { ride ->
                _uiState.update {
                    it.copy(
                        currentRideId = ride.id,
                        isLoading = false
                    )
                }
                observeRide(ride.id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        status = BookingStatus.IDLE,
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không tạo được chuyến xe"
                    )
                }
            }
        }
    }

    // Tạo order MoMo qua backend để lấy deeplink/payUrl mở app MoMo UAT.
    fun beginMomoGatewayPayment() {
        val pickup = _uiState.value.pickupLatLng
        val dropoff = _uiState.value.dropoffLatLng
        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn đủ điểm đón và điểm đến") }
            return
        }

        val amount = (_uiState.value.finalPrice.takeIf { it > 0.0 } ?: _uiState.value.price).toLong()
        if (amount <= 0L) {
            _uiState.update { it.copy(error = "Số tiền không hợp lệ") }
            return
        }

        viewModelScope.launch {
            val orderId = "WW-" + System.currentTimeMillis()
            _uiState.update { it.copy(momoStatus = "CREATING", momoOrderId = orderId, momoPayUrl = null, momoMessage = null) }

            runCatching {
                momoPaymentRepository.createOrder(
                    orderId = orderId,
                    amount = amount,
                    orderInfo = "WayWay ${_uiState.value.selectedServiceType.uppercase()}",
                    extraData = "",
                    userId = authRepository.currentUser?.uid
                )
            }.onSuccess { res ->
                val payUrl = res.deeplink?.takeIf { it.isNotBlank() }
                    ?: res.payUrl?.takeIf { it.isNotBlank() }
                if (payUrl == null) {
                    _uiState.update { it.copy(momoStatus = "FAILED", momoMessage = "Không lấy được payUrl từ backend") }
                } else {
                    _uiState.update { it.copy(momoStatus = "WAITING", momoPayUrl = payUrl, momoMessage = null) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(momoStatus = "FAILED", momoMessage = e.localizedMessage ?: "Tạo thanh toán MoMo thất bại") }
            }
        }
    }

    // Luồng test dev: tạo order MoMo rồi gọi backend confirm để không cần thanh toán thật.
    fun payWithMomoTestGateway() {
        val pickup = _uiState.value.pickupLatLng
        val dropoff = _uiState.value.dropoffLatLng
        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn đủ điểm đón và điểm đến") }
            return
        }

        val amount = (_uiState.value.finalPrice.takeIf { it > 0.0 } ?: _uiState.value.price).toLong()
        if (amount <= 0L) {
            _uiState.update { it.copy(error = "Số tiền không hợp lệ") }
            return
        }

        viewModelScope.launch {
            val orderId = "WW-TEST-" + System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    momoStatus = "CREATING",
                    momoOrderId = orderId,
                    momoPayUrl = null,
                    momoMessage = "Creating MoMo test order..."
                )
            }

            runCatching {
                momoPaymentRepository.createOrder(
                    orderId = orderId,
                    amount = amount,
                    orderInfo = "WayWay ${_uiState.value.selectedServiceType.uppercase()}",
                    extraData = "",
                    userId = authRepository.currentUser?.uid
                )
                momoPaymentRepository.devConfirm(orderId)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        momoStatus = "PAID",
                        momoMessage = "MoMo test payment confirmed"
                    )
                }
                startBooking("momo_gateway")
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        momoStatus = "FAILED",
                        momoMessage = e.localizedMessage ?: "MoMo test payment failed"
                    )
                }
            }
        }
    }

    // Kiểm tra lại trạng thái đơn MoMo hiện tại khi user quay về app.
    fun pollMomoGatewayStatusOnce() {
        val orderId = _uiState.value.momoOrderId ?: return
        pollMomoGatewayStatus(orderId)
    }

    // Nhận dữ liệu từ deep link wayway://momo-return sau khi MoMo redirect về backend.
    fun handleMomoReturn(orderId: String?, resultCode: String?, message: String?) {
        if (orderId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    momoStatus = "FAILED",
                    momoMessage = message ?: "MoMo return missing orderId"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                momoOrderId = orderId,
                momoStatus = if (resultCode == "0") "WAITING" else "FAILED",
                momoMessage = message
            )
        }
        pollMomoGatewayStatus(orderId)
    }

    // Gọi backend lấy order theo orderId, nếu PAID thì tạo chuyến xe trên Firestore.
    private fun pollMomoGatewayStatus(orderId: String) {
        viewModelScope.launch {
            runCatching { momoPaymentRepository.getOrder(orderId) }
                .onSuccess { order ->
                    val status = order?.status?.uppercase() ?: "WAITING"
                    if (status == "PAID") {
                        _uiState.update { it.copy(momoStatus = "PAID", momoMessage = order?.message) }
                        startBooking("momo_gateway")
                    } else if (status == "FAILED") {
                        _uiState.update { it.copy(momoStatus = "FAILED", momoMessage = order?.message ?: "Thanh toán thất bại") }
                    } else {
                        _uiState.update { it.copy(momoStatus = "WAITING", momoMessage = order?.message) }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(momoStatus = "WAITING", momoMessage = e.localizedMessage ?: "Không kiểm tra được trạng thái") }
                }
        }
    }

    // Nút dev confirm dùng riêng khi test luồng thanh toán MoMo trong đồ án.
    fun devConfirmMomoGatewayPayment() {
        val orderId = _uiState.value.momoOrderId ?: return
        viewModelScope.launch {
            runCatching { momoPaymentRepository.devConfirm(orderId) }
                .onSuccess {
                    _uiState.update { it.copy(momoStatus = "PAID", momoMessage = "DEV confirmed payment") }
                    startBooking("momo_gateway")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(momoStatus = "FAILED", momoMessage = e.localizedMessage ?: "Dev confirm failed") }
                }
        }
    }

    // Theo dõi realtime ride để user thấy tài xế nhận chuyến, đến điểm đón và hoàn thành.
    private fun observeRide(rideId: String) {
        rideJob?.cancel()
        rideJob = viewModelScope.launch {
            rideRepository.observeRide(rideId).collect { ride ->
                if (ride == null) return@collect
                when (ride.status) {
                    "searching" -> _uiState.update { it.copy(status = BookingStatus.FINDING) }
                    "accepted", "arrived", "in_progress" -> {
                        val previousStatus = _uiState.value.rideStatus
                        _uiState.update {
                            it.copy(
                                status = BookingStatus.ON_TRIP,
                                driverName = ride.driverName.ifBlank { it.driverName },
                                driverPhone = ride.driverPhone.ifBlank { it.driverPhone },
                                driverPlate = ride.driverPlate.ifBlank { it.driverPlate },
                                rideStatus = ride.status,
                                ridePhase = when (ride.status) {
                                    "accepted" -> "Tài xế đang đến điểm đón"
                                    "arrived" -> "Tài xế đã tới điểm đón"
                                    "in_progress" -> "Đang trên đường đến điểm trả"
                                    else -> it.ridePhase
                                },
                                etaToPickup = if (ride.status == "arrived") "Đã đến" else it.etaToPickup
                            )
                        }
                        if (previousStatus != ride.status) {
                            lastTrackingRouteAt = 0L
                        }
                        observeDriverLocation(ride.driverId)
                        keepBookingPolylineVisible()
                        startTrackingRouteUpdates()
                        updateTrackingRoute(force = true)
                    }
                    "completed" -> {
                        driverLocationJob?.cancel()
                        trackingRouteJob?.cancel()
                        lastTrackingRouteAt = 0L
                        _uiState.update {
                            it.copy(
                                status = BookingStatus.COMPLETED,
                                rideStatus = ride.status,
                                ridePhase = "Chuyến xe đã hoàn thành",
                                etaToPickup = "",
                                etaToDropoff = ""
                            )
                        }
                    }
                    "cancelled" -> _uiState.update {
                        driverLocationJob?.cancel()
                        trackingRouteJob?.cancel()
                        lastTrackingRouteAt = 0L
                        it.copy(
                            status = BookingStatus.IDLE,
                            currentRideId = null,
                            driverLatLng = null,
                            ridePhase = "",
                            rideStatus = "",
                            etaToPickup = "",
                            etaToDropoff = "",
                            error = "Chuyến xe đã bị huỷ"
                        )
                    }
                }
            }
        }
    }

    // Bắt đầu vòng lặp cập nhật tuyến đường giữa tài xế và điểm cần đến.
    private fun startTrackingRouteUpdates() {
        if (trackingRouteJob?.isActive == true) return
        trackingRouteJob = viewModelScope.launch {
            while (true) {
                updateTrackingRoute(force = true)
                delay(15_000)
            }
        }
    }

    // Tính lại polyline tài xế -> điểm đón hoặc tài xế -> điểm trả theo trạng thái chuyến.
    private suspend fun updateTrackingRoute(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastTrackingRouteAt < 15_000L) return

        val state = _uiState.value
        val driver = state.driverLatLng ?: return
        if (driver.latitude == 0.0 && driver.longitude == 0.0) {
            keepBookingPolylineVisible()
            if (state.rideStatus == "arrived") {
                _uiState.update { it.copy(etaToPickup = "Đã đến") }
            }
            return
        }
        val destination = when (state.rideStatus) {
            "accepted" -> state.pickupLatLng
            "arrived" -> state.pickupLatLng
            "in_progress" -> state.dropoffLatLng
            else -> null
        } ?: return

        runCatching {
            val coordinates = "${driver.longitude},${driver.latitude};${destination.longitude},${destination.latitude}"
            val response = osrmApi.getRoute(coordinates)
            response.routes.firstOrNull()
        }.onSuccess { route ->
            route ?: return@onSuccess
            lastTrackingRouteAt = System.currentTimeMillis()
            val points = PolyUtil.decode(route.geometry)
            val eta = formatDuration(route.duration)
            _uiState.update { current ->
                when (current.rideStatus) {
                    "accepted" -> current.copy(
                        polylinePoints = points,
                        distance = String.format("%.1f km", route.distance / 1000),
                        etaToPickup = eta
                    )
                    "arrived" -> current.copy(
                        polylinePoints = points,
                        distance = String.format("%.1f km", route.distance / 1000),
                        etaToPickup = "Đã đến"
                    )
                    "in_progress" -> current.copy(
                        polylinePoints = points,
                        distance = String.format("%.1f km", route.distance / 1000),
                        etaToDropoff = eta
                    )
                    else -> current
                }
            }
        }.onFailure { throwable ->
            keepBookingPolylineVisible()
            Log.e(TAG, "Tracking route update failed: ${throwable.message}", throwable)
        }
    }

    // Giữ tuyến pickup -> dropoff để bản đồ không trống khi chưa có vị trí tài xế hợp lệ.
    private fun keepBookingPolylineVisible() {
        if (_uiState.value.polylinePoints.isNotEmpty()) return
        if (bookingPolylinePoints.isNotEmpty()) {
            _uiState.update { it.copy(polylinePoints = bookingPolylinePoints) }
            return
        }

        val pickup = _uiState.value.pickupLatLng ?: return
        val dropoff = _uiState.value.dropoffLatLng ?: return
        viewModelScope.launch {
            runCatching {
                val coordinates = "${pickup.longitude},${pickup.latitude};${dropoff.longitude},${dropoff.latitude}"
                osrmApi.getRoute(coordinates).routes.firstOrNull()
            }.onSuccess { route ->
                route ?: return@onSuccess
                val points = PolyUtil.decode(route.geometry)
                bookingPolylinePoints = points
                _uiState.update { current ->
                    if (current.polylinePoints.isEmpty()) {
                        current.copy(polylinePoints = points)
                    } else {
                        current
                    }
                }
            }
        }
    }

    // Lắng nghe realtime vị trí tài xế từ driver_locations/{driverId}.
    private fun observeDriverLocation(driverId: String) {
        if (driverId.isBlank()) return
        driverLocationJob?.cancel()
        driverLocationJob = viewModelScope.launch {
            driverLocationRepository.observeDriverLocation(driverId)
                .collect { driverLocation ->
                    driverLocation ?: return@collect
                    _uiState.update {
                        it.copy(
                            driverLatLng = LatLng(
                                driverLocation.latitude,
                                driverLocation.longitude
                            )
                        )
                    }
                    updateTrackingRoute(force = lastTrackingRouteAt == 0L)
                }
        }
    }


    // Reset màn đặt xe về trạng thái ban đầu sau khi hoàn tất hoặc huỷ chuyến.
    fun resetBooking() {
        Log.d(TAG, "Resetting booking screen")
        driverLocationJob?.cancel()
        trackingRouteJob?.cancel()
        lastTrackingRouteAt = 0L
        _uiState.update {
            BikeState(
                currentLatLng = it.currentLatLng,
                pickupLatLng = it.currentLatLng,
                pickupAddress = it.pickupAddress
            )
        }
        bookingPolylinePoints = emptyList()
    }


    // Nhận vị trí user chấm trên map và cập nhật pickup/dropoff tương ứng.
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
    // Huỷ chuyến hiện tại trên Firestore và xoá state theo dõi tài xế ở máy user.
    fun cancelBooking() {
        Log.i(TAG, "Booking canceled")
        val rideId = _uiState.value.currentRideId
        rideJob?.cancel()
        driverLocationJob?.cancel()
        trackingRouteJob?.cancel()
        lastTrackingRouteAt = 0L
        if (rideId != null) {
            viewModelScope.launch {
                runCatching { rideRepository.cancelRide(rideId) }
            }
        }
        _uiState.update {
            it.copy(
                status = BookingStatus.IDLE,
                currentRideId = null,
                driverLatLng = null,
                ridePhase = "",
                rideStatus = "",
                etaToPickup = "",
                etaToDropoff = ""
            )
        }
        bookingPolylinePoints = emptyList()
    }
    // Áp dụng mã giảm giá đã chọn vào giá cước hiện tại.
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
    // Tải danh sách mã giảm giá từ Firestore để user chọn trước khi đặt xe.
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

    // Dọn các listener khi màn hình bị huỷ để tránh rò rỉ realtime listener.
    override fun onCleared() {
        super.onCleared()
        rideJob?.cancel()
        driverLocationJob?.cancel()
        trackingRouteJob?.cancel()
        lastTrackingRouteAt = 0L
    }

    // Đổi giây từ OSRM sang chuỗi phút để hiển thị ETA.
    private fun formatDuration(durationSeconds: Double): String {
        val minutes = (durationSeconds / 60.0).toInt().coerceAtLeast(1)
        return "$minutes phút"
    }

    private fun calculateBikePrice(distanceKm: Double): Double {
        return floor(distanceKm) * 5000 + 12000
    }

    private fun calculateCarPrice(distanceKm: Double): Double {
        val firstTwoKmPrice = 36_000.0
        if (distanceKm <= 2.0) return firstTwoKmPrice
        val extraKm = ceil(distanceKm - 2.0)
        return firstTwoKmPrice + extraKm * 8_000.0
    }
}
