package com.example.waywayapp.ui.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel màn hồ sơ, dùng chung cho user/admin/driver.
class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    // State chứa profile user, profile driver nếu có, loading và message.
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // Tải profile từ Firestore, nếu role DRIVER thì tải thêm hồ sơ tài xế.
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val user = repository.loadUserProfile()
                val driver = if (user.role.uppercase() == "DRIVER") {
                    repository.loadDriverProfile()
                } else {
                    null
                }
                user to driver
            }.onSuccess { (user, driver) ->
                _uiState.update {
                    it.copy(
                        user = user,
                        driver = driver,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không tải được hồ sơ"
                    )
                }
            }
        }
    }

    // Cập nhật tên trong form và đồng bộ sang driver profile nếu là tài xế.
    fun onNameChange(value: String) = updateUser { copy(name = value) }
    fun onPhoneChange(value: String) = updateUser { copy(phone = value) }
    fun onVehicleTypeChange(value: String) = updateDriver { copy(vehicleType = value) }
    fun onPlateNumberChange(value: String) = updateDriver { copy(plateNumber = value.uppercase()) }

    // Lưu hồ sơ user và driver nếu có vào Firestore.
    fun saveProfile() {
        val state = _uiState.value
        if (state.user.name.isBlank()) {
            _uiState.update { it.copy(error = "Tên không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            runCatching {
                repository.saveUserProfile(state.user)
                state.driver?.let { repository.saveDriverProfile(it) }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Đã lưu hồ sơ"
                    )
                }
                loadProfile()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.localizedMessage ?: "Không lưu được hồ sơ"
                    )
                }
            }
        }
    }

    // Đăng xuất tài khoản hiện tại.
    fun signOut() {
        repository.signOut()
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.user.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Email không khả dụng để đổi mật khẩu") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            runCatching {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).awaitTask()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Đã gửi email đổi mật khẩu"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.localizedMessage ?: "Không gửi được email đổi mật khẩu"
                    )
                }
            }
        }
    }

    // Xoá message/error sau khi UI đã hiển thị.
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    // Cập nhật bản nháp user trong state.
    private fun updateUser(
        block: AdminUser.() -> AdminUser
    ) {
        _uiState.update { it.copy(user = it.user.block()) }
        syncDriverFromUser()
    }

    // Cập nhật bản nháp driver trong state.
    private fun updateDriver(
        block: AdminDriver.() -> AdminDriver
    ) {
        _uiState.update {
            it.copy(driver = it.driver?.block())
        }
    }

    // Khi đổi tên/sđt/email user thì giữ driver profile khớp thông tin đó.
    private fun syncDriverFromUser() {
        _uiState.update { state ->
            state.copy(
                driver = state.driver?.copy(
                    name = state.user.name,
                    phone = state.user.phone,
                    email = state.user.email
                )
            )
        }
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result) {}
        }
        addOnFailureListener { throwable ->
            continuation.resumeWith(Result.failure(throwable))
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
