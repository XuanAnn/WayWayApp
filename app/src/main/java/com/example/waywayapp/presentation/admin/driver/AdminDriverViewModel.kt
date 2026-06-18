package com.example.waywayapp.presentation.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.domain.model.AdminPromo
import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser
import com.example.waywayapp.data.repository.AdminRepository
import com.example.waywayapp.data.repository.AdminPromoRepository
import com.example.waywayapp.data.repository.RideRepository
import com.example.waywayapp.domain.model.Ride
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel mÃ n admin: xem, thÃªm, sá»­a, xoÃ¡ user vÃ  driver.
class AdminDriverViewModel(
    private val repository: AdminRepository = AdminRepository(),
    private val promoRepository: AdminPromoRepository = AdminPromoRepository(),
    private val rideRepository: RideRepository = RideRepository()
) : ViewModel() {
    // State chá»©a danh sÃ¡ch, form detail vÃ  tráº¡ng thÃ¡i lÆ°u/xoÃ¡ cá»§a admin.
    private val _uiState = MutableStateFlow(AdminDriverState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUsers()
        observeDrivers()
        observePromos()
        observeRides()
    }

    // Theo dÃµi realtime collection users Ä‘á»ƒ dashboard admin luÃ´n má»›i.
    private fun observeUsers() {
        viewModelScope.launch {
            repository.observeUsers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingUsers = false,
                            message = throwable.localizedMessage ?: "KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch ngÆ°á»i dÃ¹ng"
                        )
                    }
                }
                .collect { users ->
                    _uiState.update {
                        it.copy(
                            users = users.sortedWith(
                                compareByDescending<AdminUser> { user -> user.role == "ADMIN" }
                                    .thenBy { user -> user.name.ifBlank { user.email } }
                            ),
                            isLoadingUsers = false
                        )
                    }
                }
        }
    }

    // Theo dÃµi realtime collection drivers Ä‘á»ƒ admin tháº¥y tÃ i xáº¿ má»›i/sá»­a ngay.
    private fun observeDrivers() {
        viewModelScope.launch {
            repository.observeDrivers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingDrivers = false,
                            message = throwable.localizedMessage ?: "KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch tÃ i xáº¿"
                        )
                    }
                }
                .collect { drivers ->
                    _uiState.update {
                        it.copy(
                            drivers = drivers.sortedBy { driver -> driver.name.ifBlank { driver.phone } },
                            isLoadingDrivers = false
                        )
                    }
                }
        }
    }

    private fun observePromos() {
        viewModelScope.launch {
            promoRepository.observePromos()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingPromos = false,
                            message = throwable.localizedMessage ?: "KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch promo"
                        )
                    }
                }
                .collect { promos ->
                    _uiState.update {
                        it.copy(
                            promos = promos.sortedByDescending { promo -> promo.updatedAt },
                            isLoadingPromos = false
                        )
                    }
                }
        }
    }

    private fun observeRides() {
        viewModelScope.launch {
            rideRepository.observeAllRides()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingRides = false,
                            message = throwable.localizedMessage ?: "KhÃ´ng táº£i Ä‘Æ°á»£c danh sÃ¡ch chuyáº¿n"
                        )
                    }
                }
                .collect { rides ->
                    _uiState.update {
                        it.copy(
                            rides = rides,
                            isLoadingRides = false
                        )
                    }
                }
        }
    }

    // Chuyá»ƒn tab Users/Drivers trÃªn dashboard admin.
    fun selectSection(section: AdminSection) {
        _uiState.update {
            it.copy(selectedSection = section, message = null)
        }
    }

    // LÆ°u keyword tÃ¬m kiáº¿m Ä‘á»ƒ UI lá»c danh sÃ¡ch.
    fun onSearchChange(value: String) {
        _uiState.update {
            it.copy(searchQuery = value)
        }
    }

    // Má»Ÿ detail user Ä‘Æ°á»£c chá»n Ä‘á»ƒ chá»‰nh sá»­a.
    fun editUser(user: AdminUser) {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.USERS,
                editingUser = user,
                isUserDetailOpen = true,
                message = null
            )
        }
    }

    // Má»Ÿ form táº¡o user má»›i.
    fun newUser() {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.USERS,
                editingUser = AdminUser(),
                isUserDetailOpen = true,
                message = null
            )
        }
    }

    // ÄÃ³ng detail user vÃ  xoÃ¡ dá»¯ liá»‡u form táº¡m.
    fun closeUserDetail() {
        _uiState.update {
            it.copy(
                editingUser = AdminUser(),
                isUserDetailOpen = false
            )
        }
    }

    fun onUserNameChange(value: String) = updateUser { copy(name = value) }
    fun onUserEmailChange(value: String) = updateUser { copy(email = value.trim()) }
    fun onUserPhoneChange(value: String) = updateUser { copy(phone = value) }
    fun onUserRoleChange(value: String) = updateUser { copy(role = value.uppercase()) }
    fun onUserActiveChange(value: Boolean) = updateUser { copy(isActive = value) }

    // LÆ°u user vÃ o Firestore thÃ´ng qua AdminRepository.
    fun saveUser() {
        val user = _uiState.value.editingUser
        if (user.name.isBlank() || user.email.isBlank()) {
            _uiState.update {
                it.copy(message = "TÃªn vÃ  email ngÆ°á»i dÃ¹ng lÃ  báº¯t buá»™c")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                repository.saveUser(user)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingUser = AdminUser(),
                        isUserDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ lÆ°u ngÆ°á»i dÃ¹ng"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng lÆ°u Ä‘Æ°á»£c ngÆ°á»i dÃ¹ng"
                    )
                }
            }
        }
    }

    // XoÃ¡ user Ä‘ang má»Ÿ detail sau khi UI Ä‘Ã£ xÃ¡c nháº­n.
    fun deleteUser() {
        val user = _uiState.value.editingUser
        if (user.id.isBlank()) {
            _uiState.update { it.copy(message = "Chá»n ngÆ°á»i dÃ¹ng cáº§n xoÃ¡ trÆ°á»›c") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                repository.deleteUser(user.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingUser = AdminUser(),
                        isUserDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ xoÃ¡ há»“ sÆ¡ ngÆ°á»i dÃ¹ng"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng xoÃ¡ Ä‘Æ°á»£c ngÆ°á»i dÃ¹ng"
                    )
                }
            }
        }
    }

    // Má»Ÿ detail driver Ä‘Æ°á»£c chá»n Ä‘á»ƒ chá»‰nh sá»­a.
    fun editDriver(driver: AdminDriver) {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.DRIVERS,
                editingDriver = driver,
                isDriverDetailOpen = true,
                message = null
            )
        }
    }

    // Má»Ÿ form táº¡o tÃ i xáº¿ má»›i.
    fun newDriver() {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.DRIVERS,
                editingDriver = AdminDriver(),
                isDriverDetailOpen = true,
                message = null
            )
        }
    }

    fun editPromo(promo: AdminPromo) {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.PROMOS,
                editingPromo = promo,
                isPromoDetailOpen = true,
                message = null
            )
        }
    }

    fun newPromo() {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.PROMOS,
                editingPromo = AdminPromo(),
                isPromoDetailOpen = true,
                message = null
            )
        }
    }

    fun closePromoDetail() {
        _uiState.update {
            it.copy(
                editingPromo = AdminPromo(),
                isPromoDetailOpen = false
            )
        }
    }

    fun editRide(ride: Ride) {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.RIDES,
                editingRide = ride,
                isRideDetailOpen = true,
                message = null
            )
        }
    }

    fun newRide() {
        _uiState.update {
            it.copy(
                selectedSection = AdminSection.RIDES,
                editingRide = Ride(),
                isRideDetailOpen = true,
                message = null
            )
        }
    }

    fun closeRideDetail() {
        _uiState.update {
            it.copy(
                editingRide = Ride(),
                isRideDetailOpen = false
            )
        }
    }

    // ÄÃ³ng detail driver vÃ  xoÃ¡ dá»¯ liá»‡u form táº¡m.
    fun closeDriverDetail() {
        _uiState.update {
            it.copy(
                editingDriver = AdminDriver(),
                isDriverDetailOpen = false
            )
        }
    }

    fun onDriverNameChange(value: String) = updateDriver { copy(name = value) }
    fun onDriverPhoneChange(value: String) = updateDriver { copy(phone = value) }
    fun onDriverEmailChange(value: String) = updateDriver { copy(email = value.trim()) }
    fun onVehicleTypeChange(value: String) = updateDriver { copy(vehicleType = value) }
    fun onPlateNumberChange(value: String) = updateDriver { copy(plateNumber = value.uppercase()) }
    fun onDriverRatingChange(value: String) = updateDriver {
        copy(rating = value.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: rating)
    }
    fun onDriverActiveChange(value: Boolean) = updateDriver { copy(isActive = value) }
    fun onDriverAvailableChange(value: Boolean) = updateDriver { copy(isAvailable = value) }
    fun onDriverOnlineChange(value: Boolean) = updateDriver { copy(isOnline = value) }

    fun onPromoCodeChange(value: String) = updatePromo { copy(code = value.uppercase()) }
    fun onPromoTitleChange(value: String) = updatePromo { copy(title = value) }
    fun onPromoActiveChange(value: Boolean) = updatePromo { copy(active = value) }
    fun onPromoDiscountTypeChange(value: String) = updatePromo { copy(discountType = value) }
    fun onPromoDiscountValueChange(value: String) = updatePromo {
        val parsed = value.toIntOrNull()?.coerceAtLeast(0) ?: discountValue
        copy(
            discountValue = parsed,
            percent = if (discountType.equals("percent", ignoreCase = true)) parsed else percent,
            amount = if (discountType.equals("fixed", ignoreCase = true)) parsed else amount
        )
    }
    fun onPromoMinPriceChange(value: String) = updatePromo { copy(minPrice = value.toIntOrNull()?.coerceAtLeast(0)) }
    fun onPromoMaxDiscountChange(value: String) = updatePromo { copy(maxDiscount = value.toIntOrNull()?.coerceAtLeast(0)) }
    fun onPromoServiceTypesChange(value: String) = updatePromo {
        copy(serviceTypes = value.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() })
    }
    fun onPromoStartAtChange(value: String) = updatePromo { copy(startAt = value.toLongOrNull() ?: startAt) }
    fun onPromoEndAtChange(value: String) = updatePromo { copy(endAt = value.toLongOrNull() ?: endAt) }

    fun onRideStatusChange(value: String) = updateRide { copy(status = value) }
    fun onRidePaymentStatusChange(value: String) = updateRide { copy(paymentStatus = value) }
    fun onRidePaymentMethodChange(value: String) = updateRide { copy(paymentMethod = value) }

    // LÆ°u driver vÃ o Firestore vÃ  Ä‘á»“ng bá»™ role DRIVER náº¿u cÃ³ user tÆ°Æ¡ng á»©ng.
    fun saveDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.name.isBlank() || driver.phone.isBlank() || driver.plateNumber.isBlank()) {
            _uiState.update {
                it.copy(message = "TÃªn, sá»‘ Ä‘iá»‡n thoáº¡i vÃ  biá»ƒn sá»‘ tÃ i xáº¿ lÃ  báº¯t buá»™c")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                repository.saveDriver(driver)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingDriver = AdminDriver(),
                        isDriverDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ lÆ°u tÃ i xáº¿"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng lÆ°u Ä‘Æ°á»£c tÃ i xáº¿"
                    )
                }
            }
        }
    }

    fun savePromo() {
        val promo = _uiState.value.editingPromo
        if (promo.code.isBlank() || promo.title.isBlank()) {
            _uiState.update { it.copy(message = "MÃ£ promo vÃ  tiÃªu Ä‘á» lÃ  báº¯t buá»™c") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                promoRepository.savePromo(promo)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingPromo = AdminPromo(),
                        isPromoDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ lÆ°u promo"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng lÆ°u Ä‘Æ°á»£c promo"
                    )
                }
            }
        }
    }

    fun deletePromo() {
        val promo = _uiState.value.editingPromo
        if (promo.code.isBlank()) {
            _uiState.update { it.copy(message = "Chá»n promo cáº§n xoÃ¡ trÆ°á»›c") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                promoRepository.deletePromo(promo.code)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingPromo = AdminPromo(),
                        isPromoDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ xoÃ¡ promo"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng xoÃ¡ Ä‘Æ°á»£c promo"
                    )
                }
            }
        }
    }

    fun saveRide() {
        val ride = _uiState.value.editingRide
        if (ride.id.isBlank()) {
            _uiState.update { it.copy(message = "Ride admin hiá»‡n chá»‰ há»— trá»£ cáº­p nháº­t chuyáº¿n cÃ³ sáºµn") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                rideRepository.updateRideAdminFields(
                    rideId = ride.id,
                    driverId = ride.driverId,
                    status = ride.status,
                    paymentStatus = ride.paymentStatus,
                    paymentMethod = ride.paymentMethod
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingRide = Ride(),
                        isRideDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ cáº­p nháº­t chuyáº¿n xe"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng cáº­p nháº­t Ä‘Æ°á»£c chuyáº¿n xe"
                    )
                }
            }
        }
    }

    fun deleteRide() {
        val ride = _uiState.value.editingRide
        if (ride.id.isBlank()) {
            _uiState.update { it.copy(message = "Chá»n chuyáº¿n cáº§n xoÃ¡ trÆ°á»›c") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                rideRepository.cancelRide(ride.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingRide = Ride(),
                        isRideDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ huá»· chuyáº¿n"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng huá»· Ä‘Æ°á»£c chuyáº¿n"
                    )
                }
            }
        }
    }

    // XoÃ¡ driver Ä‘ang má»Ÿ detail sau khi UI Ä‘Ã£ xÃ¡c nháº­n.
    fun deleteDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.id.isBlank()) {
            _uiState.update { it.copy(message = "Chá»n tÃ i xáº¿ cáº§n xoÃ¡ trÆ°á»›c") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                repository.deleteDriver(driver.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingDriver = AdminDriver(),
                        isDriverDetailOpen = false,
                        isSaving = false,
                        message = "ÄÃ£ xoÃ¡ tÃ i xáº¿"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "KhÃ´ng xoÃ¡ Ä‘Æ°á»£c tÃ i xáº¿"
                    )
                }
            }
        }
    }

    // XoÃ¡ snackbar/toast message sau khi Ä‘Ã£ hiá»ƒn thá»‹.
    fun clearMessage() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    // Cáº­p nháº­t báº£n nhÃ¡p user trong form detail.
    private fun updateUser(block: AdminUser.() -> AdminUser) {
        _uiState.update {
            it.copy(editingUser = it.editingUser.block())
        }
    }

    // Cáº­p nháº­t báº£n nhÃ¡p driver trong form detail.
    private fun updateDriver(block: AdminDriver.() -> AdminDriver) {
        _uiState.update {
            it.copy(editingDriver = it.editingDriver.block())
        }
    }

    private fun updatePromo(block: AdminPromo.() -> AdminPromo) {
        _uiState.update {
            it.copy(editingPromo = it.editingPromo.block())
        }
    }

    private fun updateRide(block: Ride.() -> Ride) {
        _uiState.update {
            it.copy(editingRide = it.editingRide.block())
        }
    }
}

