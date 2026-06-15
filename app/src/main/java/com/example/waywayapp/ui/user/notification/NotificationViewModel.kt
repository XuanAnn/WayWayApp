package com.example.waywayapp.ui.user.notification

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

class NotificationViewModel(
    private val rideRepository: RideRepository = RideRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationState())
    val uiState: StateFlow<NotificationState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.value = NotificationState(
                isLoading = false,
                error = "Bạn cần đăng nhập để xem thông báo."
            )
            return
        }

        viewModelScope.launch {
            rideRepository.observeUserRides(userId)
                .catch { throwable ->
                    _uiState.value = NotificationState(
                        isLoading = false,
                        error = throwable.message ?: "Không tải được thông báo."
                    )
                }
                .collect { rides ->
                    _uiState.value = NotificationState(
                        isLoading = false,
                        rides = rides
                    )
                }
        }
    }
}
