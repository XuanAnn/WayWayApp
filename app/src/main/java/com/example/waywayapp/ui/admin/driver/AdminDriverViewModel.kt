package com.example.waywayapp.ui.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel màn admin: xem, thêm, sửa, xoá user và driver.
class AdminDriverViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {
    // State chứa danh sách, form detail và trạng thái lưu/xoá của admin.
    private val _uiState = MutableStateFlow(AdminDriverState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUsers()
        observeDrivers()
    }

    // Theo dõi realtime collection users để dashboard admin luôn mới.
    private fun observeUsers() {
        viewModelScope.launch {
            repository.observeUsers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingUsers = false,
                            message = throwable.localizedMessage ?: "Không tải được danh sách người dùng"
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

    // Theo dõi realtime collection drivers để admin thấy tài xế mới/sửa ngay.
    private fun observeDrivers() {
        viewModelScope.launch {
            repository.observeDrivers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingDrivers = false,
                            message = throwable.localizedMessage ?: "Không tải được danh sách tài xế"
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

    // Chuyển tab Users/Drivers trên dashboard admin.
    fun selectSection(section: AdminSection) {
        _uiState.update {
            it.copy(selectedSection = section, message = null)
        }
    }

    // Lưu keyword tìm kiếm để UI lọc danh sách.
    fun onSearchChange(value: String) {
        _uiState.update {
            it.copy(searchQuery = value)
        }
    }

    // Mở detail user được chọn để chỉnh sửa.
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

    // Mở form tạo user mới.
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

    // Đóng detail user và xoá dữ liệu form tạm.
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

    // Lưu user vào Firestore thông qua AdminRepository.
    fun saveUser() {
        val user = _uiState.value.editingUser
        if (user.name.isBlank() || user.email.isBlank()) {
            _uiState.update {
                it.copy(message = "Tên và email người dùng là bắt buộc")
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
                        message = "Đã lưu người dùng"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "Không lưu được người dùng"
                    )
                }
            }
        }
    }

    // Xoá user đang mở detail sau khi UI đã xác nhận.
    fun deleteUser() {
        val user = _uiState.value.editingUser
        if (user.id.isBlank()) {
            _uiState.update { it.copy(message = "Chọn người dùng cần xoá trước") }
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
                        message = "Đã xoá hồ sơ người dùng"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "Không xoá được người dùng"
                    )
                }
            }
        }
    }

    // Mở detail driver được chọn để chỉnh sửa.
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

    // Mở form tạo tài xế mới.
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

    // Đóng detail driver và xoá dữ liệu form tạm.
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

    // Lưu driver vào Firestore và đồng bộ role DRIVER nếu có user tương ứng.
    fun saveDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.name.isBlank() || driver.phone.isBlank() || driver.plateNumber.isBlank()) {
            _uiState.update {
                it.copy(message = "Tên, số điện thoại và biển số tài xế là bắt buộc")
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
                        message = "Đã lưu tài xế"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "Không lưu được tài xế"
                    )
                }
            }
        }
    }

    // Xoá driver đang mở detail sau khi UI đã xác nhận.
    fun deleteDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.id.isBlank()) {
            _uiState.update { it.copy(message = "Chọn tài xế cần xoá trước") }
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
                        message = "Đã xoá tài xế"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = throwable.localizedMessage ?: "Không xoá được tài xế"
                    )
                }
            }
        }
    }

    // Xoá snackbar/toast message sau khi đã hiển thị.
    fun clearMessage() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    // Cập nhật bản nháp user trong form detail.
    private fun updateUser(block: AdminUser.() -> AdminUser) {
        _uiState.update {
            it.copy(editingUser = it.editingUser.block())
        }
    }

    // Cập nhật bản nháp driver trong form detail.
    private fun updateDriver(block: AdminDriver.() -> AdminDriver) {
        _uiState.update {
            it.copy(editingDriver = it.editingDriver.block())
        }
    }
}
