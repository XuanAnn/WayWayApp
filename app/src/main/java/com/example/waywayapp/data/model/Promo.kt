package com.example.waywayapp.data.model

// Model biểu diễn mã giảm giá lấy từ Firestore hoặc dữ liệu mẫu.
data class Promo(
    val code: String,
    val title: String = "",
    val active: Boolean = true,
    val percent: Int? = null,
    val amount: Int? = null,
    val maxDiscount: Int? = null,
    val minPrice: Int? = null,
    val serviceTypes: List<String> = emptyList(),
    val startAt: Long = 0L,
    val endAt: Long = Long.MAX_VALUE
) {
    // Tính số tiền giảm thực tế theo giá cuốc hiện tại.
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

    fun isActiveFor(
        serviceType: String,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        // Kiểm tra mã còn hạn và có áp dụng cho dịch vụ hiện tại hay không.
        val matchesService = serviceTypes.isEmpty() || serviceTypes.any {
            it.equals(serviceType, ignoreCase = true)
        }
        return active && matchesService && now in startAt..endAt
    }
}
