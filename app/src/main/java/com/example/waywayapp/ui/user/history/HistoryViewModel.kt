package com.example.waywayapp.ui.user.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.RideRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// ViewModel hiển thị lịch sử cuốc xe của user hiện tại.
class HistoryViewModel(
    private val rideRepository: RideRepository = RideRepository()
) : ViewModel() {
    // State chứa danh sách ride, loading và lỗi của màn lịch sử.
    private val _uiState = MutableStateFlow(HistoryState())
    val uiState: StateFlow<HistoryState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    // Lắng nghe realtime các ride của user từ collection rides.
    private fun observeHistory() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.value = HistoryState(
                isLoading = false,
                error = "Bạn cần đăng nhập để xem lịch sử cuốc."
            )
            return
        }

        viewModelScope.launch {
            rideRepository.observeUserRides(userId)
                .catch { throwable ->
                    _uiState.value = HistoryState(
                        isLoading = false,
                        error = throwable.message ?: "Không tải được lịch sử cuốc."
                    )
                }
                .collect { rides ->
                    _uiState.value = HistoryState(
                        isLoading = false,
                        rides = rides
                    )
                }
        }
    }
}
