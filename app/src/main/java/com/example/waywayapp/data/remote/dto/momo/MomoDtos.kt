package com.example.waywayapp.data.remote.dto.momo

data class MomoCreateOrderRequest(
    val orderId: String,
    val amount: Long,
    val orderInfo: String,
    val extraData: String = "",
    val userId: String? = null
)

data class MomoCreateOrderResponse(
    val orderId: String? = null,
    val requestId: String? = null,
    val amount: Long? = null,
    val payUrl: String? = null,
    val deeplink: String? = null,
    val qrCodeUrl: String? = null
)

data class MomoOrder(
    val orderId: String? = null,
    val amount: Long? = null,
    val orderInfo: String? = null,
    val status: String? = null,
    val transId: Long? = null,
    val resultCode: Int? = null,
    val message: String? = null,
    val updatedAt: Long? = null
)

data class MomoOrderStatusResponse(
    val order: MomoOrder? = null
)

