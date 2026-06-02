package com.example.waywayapp.data.repository

import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.data.model.Promo
import com.example.waywayapp.data.remote.dto.ai.AiAssistantContextDto
import com.example.waywayapp.data.remote.dto.ai.AiChatRequestDto
import com.example.waywayapp.data.remote.dto.ai.AiFareDto
import com.example.waywayapp.data.remote.dto.ai.AiPromoDto

// Repository trung gian gửi câu hỏi AI từ app Android lên backend Spring Boot.
class AiChatRepository {
    // API Retrofit gọi endpoint /api/ai/chat của backend.
    private val api = RetrofitProvider.aiChatApi
    // Repository lấy mã khuyến mãi đang hoạt động để đưa vào context AI.
    private val promoRepository = PromoRepository()

    // Gửi tin nhắn lên AI, kèm mã giảm giá và giá cước hiện tại nếu có.
    suspend fun send(
        message: String,
        role: String = "USER",
        serviceType: String = "bike",
        currentFare: AiFareDto? = null
    ): Pair<String, String> {
        // User cần context khuyến mãi, driver không cần danh sách promo.
        val activePromos = if (role.equals("USER", ignoreCase = true)) {
            promoRepository.getActivePromos(serviceType).map { it.toAiPromoDto() }
        } else {
            emptyList()
        }

        // Gửi message + role + context thật để backend không cho AI tự bịa giá/mã.
        val response = api.chat(
            AiChatRequestDto(
                message = message,
                role = role,
                context = AiAssistantContextDto(
                    serviceType = serviceType,
                    activePromos = activePromos,
                    currentFare = currentFare
                )
            )
        )
        return response.reply to response.mode
    }

    // Chuyển Promo trong app sang DTO gọn để gửi qua API AI.
    private fun Promo.toAiPromoDto(): AiPromoDto {
        val discountType = when {
            amount != null -> "fixed"
            percent != null -> "percent"
            else -> ""
        }
        return AiPromoDto(
            code = code,
            title = title,
            discountType = discountType,
            discountValue = amount ?: percent ?: 0,
            maxDiscount = maxDiscount,
            minFare = minPrice,
            serviceTypes = serviceTypes
        )
    }
}
