package com.example.waywayapp.ui.driver.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.data.repository.RideRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel màn thu nhập tài xế, tính ví từ các chuyến completed.
class DriverIncomeViewModel(
    private val rideRepository: RideRepository = RideRepository(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    // State chứa danh sách chuyến hoàn thành và tổng thu nhập.
    private val _uiState = MutableStateFlow(DriverIncomeState())
    val uiState: StateFlow<DriverIncomeState> = _uiState.asStateFlow()

    // Job realtime lắng nghe completed rides của tài xế hiện tại.
    private var incomeJob: Job? = null

    init {
        loadIncome()
    }

    // Tải lại thu nhập khi tài xế bấm refresh.
    fun refresh() {
        loadIncome()
    }

    // Lấy uid driver từ Firebase Auth rồi observeCompletedRides trong Firestore.
    private fun loadIncome() {
        incomeJob?.cancel()
        val driverId = authRepository.currentUser?.uid
        if (driverId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    rides = emptyList(),
                    error = "Bạn cần đăng nhập bằng tài khoản tài xế."
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        incomeJob = viewModelScope.launch {
            rideRepository.observeCompletedRides(driverId)
                .collect { rides ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rides = rides,
                            error = null
                        )
                    }
                }
        }
    }
}
