package com.example.waywayapp.data.remote.api

import com.example.waywayapp.data.remote.dto.momo.MomoCreateOrderRequest
import com.example.waywayapp.data.remote.dto.momo.MomoCreateOrderResponse
import com.example.waywayapp.data.remote.dto.momo.MomoOrderStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// API Retrofit nối app Android với backend Spring Boot phần thanh toán MoMo.
interface MomoBackendApi {
    // Gửi thông tin order lên backend để tạo payUrl/deeplink MoMo.
    @POST("api/momo/create")
    suspend fun create(@Body body: MomoCreateOrderRequest): MomoCreateOrderResponse

    // Kiểm tra trạng thái order đã được backend lưu từ IPN/return hay chưa.
    @GET("api/momo/orders/{orderId}")
    suspend fun getOrder(@Path("orderId") orderId: String): MomoOrderStatusResponse

    // Endpoint dev để tự đánh dấu order paid khi test đồ án.
    @POST("api/momo/orders/{orderId}/dev-confirm")
    suspend fun devConfirm(@Path("orderId") orderId: String): MomoOrderStatusResponse
}
