package com.example.waywayapp.ui.user.booking.food.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodOrderViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodOrderState())
    val uiState = _uiState.asStateFlow()

    fun placeOrder() {
        viewModelScope.launch {
            val cartItems = repository.getCartItems().first()

            if (cartItems.isEmpty()) {
                _uiState.update {
                    it.copy(message = "Giỏ hàng đang trống")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    status = FoodOrderStatus.FINDING_DRIVER,
                    message = "Đang tìm tài xế nhận đơn..."
                )
            }

            delay(3000)

            _uiState.update {
                it.copy(
                    status = FoodOrderStatus.DRIVER_ACCEPTED,
                    driverName = "Nguyễn Văn A",
                    driverRating = 4.9,
                    vehiclePlate = "43A1-12345",
                    estimatedTime = "15 phút",
                    message = "Tài xế đã nhận đơn"
                )
            }

            delay(3000)

            _uiState.update {
                it.copy(
                    status = FoodOrderStatus.PICKING_ORDER,
                    message = "Tài xế đang đến quán lấy món"
                )
            }

            delay(4000)

            _uiState.update {
                it.copy(
                    status = FoodOrderStatus.DELIVERING,
                    message = "Đơn hàng đang được giao"
                )
            }

            delay(5000)

            repository.clearCart()

            _uiState.update {
                it.copy(
                    status = FoodOrderStatus.COMPLETED,
                    message = "Cảm ơn bạn đã đặt hàng!"
                )
            }
        }
    }

    fun resetOrder() {
        _uiState.value = FoodOrderState()
    }
}
