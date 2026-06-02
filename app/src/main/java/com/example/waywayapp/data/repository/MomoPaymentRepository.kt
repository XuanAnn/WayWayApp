package com.example.waywayapp.data.repository

import com.example.waywayapp.core.network.ApiConstants
import com.example.waywayapp.core.network.RetrofitFactory
import com.example.waywayapp.data.remote.api.MomoBackendApi
import com.example.waywayapp.data.remote.dto.momo.MomoCreateOrderRequest
import com.example.waywayapp.data.remote.dto.momo.MomoCreateOrderResponse
import com.example.waywayapp.data.remote.dto.momo.MomoOrder

// Repository gọi backend Spring Boot để xử lý thanh toán MoMo UAT.
class MomoPaymentRepository {
    // Retrofit trỏ tới backend local/ngrok được cấu hình trong ApiConstants.
    private val api: MomoBackendApi by lazy {
        RetrofitFactory.create(ApiConstants.MOMO_BACKEND_BASE_URL).create(MomoBackendApi::class.java)
    }

    // Tạo order MoMo trên backend, backend sẽ ký HMAC và gọi MoMo gateway.
    suspend fun createOrder(
        orderId: String,
        amount: Long,
        orderInfo: String,
        extraData: String = "",
        userId: String? = null
    ): MomoCreateOrderResponse {
        return api.create(
            MomoCreateOrderRequest(
                orderId = orderId,
                amount = amount,
                orderInfo = orderInfo,
                extraData = extraData,
                userId = userId
            )
        )
    }

    // Lấy trạng thái order từ backend để app biết đã thanh toán hay chưa.
    suspend fun getOrder(orderId: String): MomoOrder? {
        return api.getOrder(orderId).order
    }

    // Xác nhận thanh toán giả lập cho môi trường demo/dev không cần qua app MoMo thật.
    suspend fun devConfirm(orderId: String): MomoOrder? {
        return api.devConfirm(orderId).order
    }
}
