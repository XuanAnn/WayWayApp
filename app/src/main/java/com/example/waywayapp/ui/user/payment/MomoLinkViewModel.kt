package com.example.waywayapp.ui.user.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.MomoPaymentRepository
import com.example.waywayapp.data.repository.MomoRepository
import com.example.waywayapp.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MomoLinkViewModel(
    private val momoRepository: MomoRepository = MomoRepository(),
    private val momoPaymentRepository: MomoPaymentRepository = MomoPaymentRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MomoLinkState())
    val uiState: StateFlow<MomoLinkState> = _uiState.asStateFlow()

    init {
        load()
    }

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
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Khong tai duoc trang thai MoMo."
                    )
                }
            }
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, error = null, message = null) }
    }

    fun link() {
        val phone = _uiState.value.phone.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "Vui long nhap so dien thoai MoMo.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            val uid = profileRepository.currentUid.orEmpty()
            val orderId = "momo-link-${uid.ifBlank { "user" }}-${System.currentTimeMillis()}"

            runCatching {
                momoPaymentRepository.createOrder(
                    orderId = orderId,
                    amount = 1000L,
                    orderInfo = "Xac thuc lien ket vi MoMo WayWay",
                    extraData = "momo_link",
                    userId = uid.ifBlank { null }
                )
            }.onSuccess { response ->
                val url = response.deeplink?.takeIf { it.isNotBlank() }
                    ?: response.payUrl?.takeIf { it.isNotBlank() }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingOrderId = orderId,
                        launchUrl = url,
                        verifyStatus = "WAITING",
                        message = if (url.isNullOrBlank()) {
                            "Da tao yeu cau xac thuc. Quay lai va bam kiem tra trang thai."
                        } else {
                            "Mo MoMo UAT de xac thuc lien ket."
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verifyStatus = "FAILED",
                        error = throwable.localizedMessage ?: "Khong tao duoc yeu cau xac thuc MoMo UAT."
                    )
                }
            }
        }
    }

    fun verifyPendingLink() {
        val orderId = _uiState.value.pendingOrderId ?: return
        val phone = _uiState.value.phone

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            runCatching {
                momoPaymentRepository.getOrder(orderId)
            }.onSuccess { order ->
                when (order?.status?.uppercase()) {
                    "PAID" -> saveVerifiedLink(phone)
                    "FAILED" -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                verifyStatus = "FAILED",
                                error = order.message ?: "Xac thuc MoMo that bai."
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                verifyStatus = "WAITING",
                                message = "Chua nhan duoc ket qua tu MoMo. Vui long thu lai sau vai giay."
                            )
                        }
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Khong kiem tra duoc trang thai MoMo."
                    )
                }
            }
        }
    }

    private suspend fun saveVerifiedLink(phone: String) {
        runCatching {
            momoRepository.linkMomoUat(phone)
        }.onSuccess {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLinked = true,
                    pendingOrderId = null,
                    verifyStatus = "PAID",
                    message = "Da xac thuc va lien ket MoMo UAT."
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    verifyStatus = "FAILED",
                    error = throwable.localizedMessage ?: "Khong luu duoc lien ket MoMo."
                )
            }
        }
    }

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
                        pendingOrderId = null,
                        verifyStatus = "IDLE",
                        message = "Da huy lien ket MoMo."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Khong huy duoc lien ket MoMo."
                    )
                }
            }
        }
    }

    fun clearLaunchUrl() {
        _uiState.update { it.copy(launchUrl = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
