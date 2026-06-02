package com.example.waywayapp.data.remote.dto.ai

data class AiChatRequestDto(
    val message: String,
    val role: String = "USER",
    val locale: String = "vi-VN",
    val context: AiAssistantContextDto = AiAssistantContextDto()
)

data class AiChatResponseDto(
    val reply: String = "",
    val mode: String = ""
)

data class AiAssistantContextDto(
    val serviceType: String = "bike",
    val activePromos: List<AiPromoDto> = emptyList(),
    val currentFare: AiFareDto? = null
)

data class AiPromoDto(
    val code: String,
    val title: String = "",
    val discountType: String = "",
    val discountValue: Int = 0,
    val maxDiscount: Int? = null,
    val minFare: Int? = null,
    val serviceTypes: List<String> = emptyList()
)

data class AiFareDto(
    val serviceType: String,
    val estimatedFare: Double,
    val finalFare: Double,
    val discount: Double = 0.0,
    val selectedPromoCode: String = ""
)
