package com.example.waywayapp.presentation.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.AdminPromoRepositoryImpl
import com.example.waywayapp.data.repository.AdminRepositoryImpl
import com.example.waywayapp.data.repository.RideRepositoryImpl
import com.example.waywayapp.domain.model.AdminPromo
import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser
import com.example.waywayapp.domain.model.Ride
import com.example.waywayapp.domain.repository.AdminRepository
import com.example.waywayapp.domain.repository.AdminPromoRepository
import com.example.waywayapp.domain.repository.RideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminDriverViewModel(
    private val repository: AdminRepositoryImpl,
    private val promoRepository: AdminPromoRepositoryImpl,
    private val rideRepository: RideRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDriverState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUsers()
        observeDrivers()
        observePromos()
        observeRides()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            repository.observeUsers()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoadingUsers = false, message = throwable.localizedMessage ?: "Lỗi tải người dùng") }
                }
                .collect { users ->
                    _uiState.update { it.copy(users = users.sortedWith(compareByDescending<AdminUser> { u -> u.role == "ADMIN" }.thenBy { u -> u.name.ifBlank { u.email } }), isLoadingUsers = false) }
                }
        }
    }

    private fun observeDrivers() {
        viewModelScope.launch {
            repository.observeDrivers()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoadingDrivers = false, message = throwable.localizedMessage ?: "Lỗi tải tài xế") }
                }
                .collect { drivers ->
                    _uiState.update { it.copy(drivers = drivers.sortedBy { it.name.ifBlank { it.phone } }, isLoadingDrivers = false) }
                }
        }
    }

    private fun observePromos() {
        viewModelScope.launch {
            promoRepository.observePromos()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoadingPromos = false, message = throwable.localizedMessage ?: "Lỗi tải khuyến mãi") }
                }
                .collect { promos ->
                    _uiState.update { it.copy(promos = promos.sortedByDescending { it.updatedAt }, isLoadingPromos = false) }
                }
        }
    }

    private fun observeRides() {
        viewModelScope.launch {
            rideRepository.observeAllRides()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoadingRides = false, message = throwable.localizedMessage ?: "Lỗi tải chuyến đi") }
                }
                .collect { rides ->
                    _uiState.update { it.copy(rides = rides, isLoadingRides = false) }
                }
        }
    }

    fun selectSection(section: AdminSection) = _uiState.update { it.copy(selectedSection = section, message = null) }
    fun onSearchChange(value: String) = _uiState.update { it.copy(searchQuery = value) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    // --- USER ACTIONS ---
    fun editUser(user: AdminUser) = _uiState.update { it.copy(selectedSection = AdminSection.USERS, editingUser = user, isUserDetailOpen = true, message = null) }
    fun newUser() = _uiState.update { it.copy(selectedSection = AdminSection.USERS, editingUser = AdminUser(), isUserDetailOpen = true, message = null) }
    fun closeUserDetail() = _uiState.update { it.copy(editingUser = AdminUser(), isUserDetailOpen = false) }

    fun onUserNameChange(value: String) = updateUser { copy(name = value) }
    fun onUserEmailChange(value: String) = updateUser { copy(email = value.trim()) }
    fun onUserPhoneChange(value: String) = updateUser { copy(phone = value) }
    fun onUserRoleChange(value: String) = updateUser { copy(role = value.uppercase()) }
    fun onUserActiveChange(value: Boolean) = updateUser { copy(isActive = value) }

    fun saveUser() {
        val user = _uiState.value.editingUser
        if (user.name.isBlank() || user.email.isBlank()) {
            _uiState.update { it.copy(message = "Tên và email là bắt buộc") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { repository.saveUser(user) }
                .onSuccess { _uiState.update { it.copy(editingUser = AdminUser(), isUserDetailOpen = false, isSaving = false, message = "Đã lưu người dùng") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi lưu người dùng") } }
        }
    }

    fun deleteUser() {
        val user = _uiState.value.editingUser
        if (user.id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { repository.deleteUser(user.id) }
                .onSuccess { _uiState.update { it.copy(editingUser = AdminUser(), isUserDetailOpen = false, isSaving = false, message = "Đã xóa người dùng") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi xóa người dùng") } }
        }
    }

    // --- DRIVER ACTIONS ---
    fun editDriver(driver: AdminDriver) = _uiState.update { it.copy(selectedSection = AdminSection.DRIVERS, editingDriver = driver, isDriverDetailOpen = true, message = null) }
    fun newDriver() = _uiState.update { it.copy(selectedSection = AdminSection.DRIVERS, editingDriver = AdminDriver(), isDriverDetailOpen = true, message = null) }
    fun closeDriverDetail() = _uiState.update { it.copy(editingDriver = AdminDriver(), isDriverDetailOpen = false) }

    fun onDriverNameChange(value: String) = updateDriver { copy(name = value) }
    fun onDriverPhoneChange(value: String) = updateDriver { copy(phone = value) }
    fun onDriverEmailChange(value: String) = updateDriver { copy(email = value.trim()) }
    fun onVehicleTypeChange(value: String) = updateDriver { copy(vehicleType = value) }
    fun onPlateNumberChange(value: String) = updateDriver { copy(plateNumber = value.uppercase()) }
    fun onDriverRatingChange(value: String) = updateDriver { copy(rating = value.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: rating) }
    fun onDriverActiveChange(value: Boolean) = updateDriver { copy(isActive = value) }
    fun onDriverAvailableChange(value: Boolean) = updateDriver { copy(isAvailable = value) }
    fun onDriverOnlineChange(value: Boolean) = updateDriver { copy(isOnline = value) }

    fun saveDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.name.isBlank() || driver.phone.isBlank() || driver.plateNumber.isBlank()) {
            _uiState.update { it.copy(message = "Tên, SĐT và biển số là bắt buộc") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { repository.saveDriver(driver) }
                .onSuccess { _uiState.update { it.copy(editingDriver = AdminDriver(), isDriverDetailOpen = false, isSaving = false, message = "Đã lưu tài xế") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi lưu tài xế") } }
        }
    }

    fun deleteDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { repository.deleteDriver(driver.id) }
                .onSuccess { _uiState.update { it.copy(editingDriver = AdminDriver(), isDriverDetailOpen = false, isSaving = false, message = "Đã xóa tài xế") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi xóa tài xế") } }
        }
    }

    // --- PROMO ACTIONS ---
    fun editPromo(promo: AdminPromo) = _uiState.update { it.copy(selectedSection = AdminSection.PROMOS, editingPromo = promo, isPromoDetailOpen = true, message = null) }
    fun newPromo() = _uiState.update { it.copy(selectedSection = AdminSection.PROMOS, editingPromo = AdminPromo(), isPromoDetailOpen = true, message = null) }
    fun closePromoDetail() = _uiState.update { it.copy(editingPromo = AdminPromo(), isPromoDetailOpen = false) }

    fun onPromoCodeChange(value: String) = updatePromo { copy(code = value.uppercase()) }
    fun onPromoTitleChange(value: String) = updatePromo { copy(title = value) }
    fun onPromoActiveChange(value: Boolean) = updatePromo { copy(active = value) }
    fun onPromoDiscountTypeChange(value: String) = updatePromo { copy(discountType = value) }
    fun onPromoDiscountValueChange(value: String) = updatePromo {
        val parsed = value.toIntOrNull()?.coerceAtLeast(0) ?: discountValue
        copy(discountValue = parsed, percent = if (discountType.equals("percent", true)) parsed else percent, amount = if (discountType.equals("fixed", true)) parsed else amount)
    }
    fun onPromoMinPriceChange(value: String) = updatePromo { copy(minPrice = value.toIntOrNull()?.coerceAtLeast(0)) }
    fun onPromoMaxDiscountChange(value: String) = updatePromo { copy(maxDiscount = value.toIntOrNull()?.coerceAtLeast(0)) }

    fun togglePromoServiceType(serviceId: String) = updatePromo {
        val current = serviceTypes.toMutableSet()
        if (current.contains(serviceId)) current.remove(serviceId) else current.add(serviceId)
        copy(serviceTypes = current.toList())
    }

    fun savePromo() {
        val promo = _uiState.value.editingPromo
        if (promo.code.isBlank() || promo.title.isBlank()) {
            _uiState.update { it.copy(message = "Mã promo và tiêu đề là bắt buộc") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { promoRepository.savePromo(promo) }
                .onSuccess { _uiState.update { it.copy(editingPromo = AdminPromo(), isPromoDetailOpen = false, isSaving = false, message = "Đã lưu promo") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi lưu promo") } }
        }
    }

    fun deletePromo() {
        val promo = _uiState.value.editingPromo
        if (promo.code.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { promoRepository.deletePromo(promo.code) }
                .onSuccess { _uiState.update { it.copy(editingPromo = AdminPromo(), isPromoDetailOpen = false, isSaving = false, message = "Đã xóa promo") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi xóa promo") } }
        }
    }

    // --- RIDE ACTIONS ---
    fun editRide(ride: Ride) = _uiState.update { it.copy(selectedSection = AdminSection.RIDES, editingRide = ride, isRideDetailOpen = true, message = null) }
    fun closeRideDetail() = _uiState.update { it.copy(editingRide = Ride(), isRideDetailOpen = false) }

    fun onRideStatusChange(value: String) = updateRide { copy(status = value) }
    fun onRidePaymentStatusChange(value: String) = updateRide { copy(paymentStatus = value) }
    fun onRidePaymentMethodChange(value: String) = updateRide { copy(paymentMethod = value) }

    fun saveRide() {
        val ride = _uiState.value.editingRide
        if (ride.id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { rideRepository.updateRideAdminFields(ride.id, ride.driverId, ride.status, ride.paymentStatus, ride.paymentMethod) }
                .onSuccess { _uiState.update { it.copy(editingRide = Ride(), isRideDetailOpen = false, isSaving = false, message = "Đã cập nhật chuyến") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi cập nhật chuyến") } }
        }
    }

    fun deleteRide() {
        val ride = _uiState.value.editingRide
        if (ride.id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { rideRepository.cancelRide(ride.id) }
                .onSuccess { _uiState.update { it.copy(editingRide = Ride(), isRideDetailOpen = false, isSaving = false, message = "Đã hủy chuyến") } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, message = t.localizedMessage ?: "Lỗi hủy chuyến") } }
        }
    }

    // --- UTILS ---
    private fun updateUser(block: AdminUser.() -> AdminUser) = _uiState.update { it.copy(editingUser = it.editingUser.block()) }
    private fun updateDriver(block: AdminDriver.() -> AdminDriver) = _uiState.update { it.copy(editingDriver = it.editingDriver.block()) }
    private fun updatePromo(block: AdminPromo.() -> AdminPromo) = _uiState.update { it.copy(editingPromo = it.editingPromo.block()) }
    private fun updateRide(block: Ride.() -> Ride) = _uiState.update { it.copy(editingRide = it.editingRide.block()) }
}