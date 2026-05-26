package com.example.waywayapp.ui.admin.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.repository.AdminDriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminDriverViewModel(
    private val repository: AdminDriverRepository = AdminDriverRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDriverState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDrivers()
    }

    private fun observeDrivers() {
        viewModelScope.launch {
            repository.observeDrivers()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = throwable.localizedMessage
                                ?: "Không tải được danh sách tài xế"
                        )
                    }
                }
                .collect { drivers ->
                    _uiState.update {
                        it.copy(
                            drivers = drivers.sortedBy { driver -> driver.name },
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun editDriver(driver: AdminDriver) {
        _uiState.update {
            it.copy(editingDriver = driver, message = null)
        }
    }

    fun newDriver() {
        _uiState.update {
            it.copy(editingDriver = AdminDriver(), message = null)
        }
    }

    fun onNameChange(value: String) = updateEditing {
        copy(name = value)
    }

    fun onPhoneChange(value: String) = updateEditing {
        copy(phone = value)
    }

    fun onEmailChange(value: String) = updateEditing {
        copy(email = value)
    }

    fun onVehicleTypeChange(value: String) = updateEditing {
        copy(vehicleType = value)
    }

    fun onPlateNumberChange(value: String) = updateEditing {
        copy(plateNumber = value)
    }

    fun onAvailableChange(value: Boolean) = updateEditing {
        copy(isAvailable = value)
    }

    fun saveDriver() {
        val driver = _uiState.value.editingDriver
        if (driver.name.isBlank() || driver.phone.isBlank()) {
            _uiState.update {
                it.copy(message = "Tên và số điện thoại là bắt buộc")
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.saveDriver(driver)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        editingDriver = AdminDriver(),
                        message = "Đã lưu tài xế"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        message = throwable.localizedMessage ?: "Không lưu được tài xế"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(message = null)
        }
    }

    private fun updateEditing(
        block: AdminDriver.() -> AdminDriver
    ) {
        _uiState.update {
            it.copy(editingDriver = it.editingDriver.block())
        }
    }
}
