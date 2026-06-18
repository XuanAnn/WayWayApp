package com.example.waywayapp.domain.model

data class AdminPromo(
    val code: String = "",
    val title: String = "",
    val active: Boolean = true,
    val discountType: String = "percent",
    val discountValue: Int = 0,
    val percent: Int? = null,
    val amount: Int? = null,
    val maxDiscount: Int? = null,
    val minPrice: Int? = null,
    val serviceTypes: List<String> = emptyList(),
    val startAt: Long = 0L,
    val endAt: Long = Long.MAX_VALUE,
    val updatedAt: Long = 0L,
    val createdAt: Long = 0L
)
