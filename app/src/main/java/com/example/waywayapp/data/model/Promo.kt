package com.example.waywayapp.data.model

data class Promo(
    val code: String,
    val percent: Int? = null,
    val amount: Int? = null,
    val maxDiscount: Int? = null,
    val minPrice: Int? = null
) {
    // Hàm tự tính toán mức giảm cho chính nó
    fun getDiscountAmount(currentPrice: Double): Double {
        if (minPrice != null && currentPrice < minPrice) return 0.0

        return when {
            amount != null -> amount.toDouble()
            percent != null -> {
                val calc = (currentPrice * percent) / 100
                maxDiscount?.let { calc.coerceAtMost(it.toDouble()) } ?: calc
            }
            else -> 0.0
        }
    }
}
