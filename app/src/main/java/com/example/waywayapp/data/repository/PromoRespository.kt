package com.example.waywayapp.data.repository

import com.example.waywayapp.data.model.Promo

class PromoRepository {
    // Giả lập dữ liệu từ API hoặc Local DB
    fun getAvailablePromos(): List<Promo> {
        return listOf(
            Promo(code = "WAYTIETKIEM", percent = 10, minPrice = 20000),
            Promo(code = "SALE20", percent = 20, maxDiscount = 15000),
            Promo(code = "GIAM10K", amount = 10000)
        )
    }

    // Tách logic tính toán ra một hàm riêng
    fun calculateDiscount(price: Double, promo: Promo): Double {
        if (promo.minPrice != null && price < promo.minPrice) return 0.0

        return when {
            promo.amount != null -> promo.amount.toDouble()
            promo.percent != null -> {
                val calc = (price * promo.percent) / 100
                if (promo.maxDiscount != null) calc.coerceAtMost(promo.maxDiscount.toDouble()) else calc
            }
            else -> 0.0
        }
    }
}