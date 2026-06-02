package com.example.waywayapp.ui.user.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.MomoRepository
import com.example.waywayapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel màn liên kết ví MoMo UAT của user.
class MomoLinkViewModel(
    private val momoRepository: MomoRepository = MomoRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    // State chứa số điện thoại ví, trạng thái đã liên kết và thông báo lỗi.
    private val _uiState = MutableStateFlow(MomoLinkState())
    val uiState: StateFlow<MomoLinkState> = _uiState.asStateFlow()

    init {
        load()
    }

    // Tải trạng thái MoMo từ profile user trên Firestore.
    fun load() {
        viewModelScope.launch {
            runCatching {
                profileRepository.loadUserProfile()
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        phone = user.momoPhone.ifBlank { user.phone },
                        isLinked = user.momoLinked,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Cập nhật số điện thoại ví MoMo trong form.
    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, error = null, message = null) }
    }

    // Gọi repository để lưu liên kết MoMo UAT vào Firestore.
    fun link() {
        val phone = _uiState.value.phone
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            runCatching {
                momoRepository.linkMomoUat(phone)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLinked = true,
                        message = "Đã liên kết ví MoMo UAT."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không liên kết được MoMo UAT."
                    )
                }
            }
        }
    }

    // Gọi repository để hủy liên kết ví MoMo UAT.
    fun unlink() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            runCatching {
                momoRepository.unlinkMomo()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLinked = false,
                        message = "Đã hủy liên kết MoMo."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không hủy được liên kết MoMo."
                    )
                }
            }
        }
    }

    // Xoá message/error sau khi UI đã hiển thị.
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
