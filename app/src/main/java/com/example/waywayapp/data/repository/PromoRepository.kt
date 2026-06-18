package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.domain.model.Promo
import kotlinx.coroutines.tasks.await

// Repository quản lý mã khuyến mãi dùng cho đặt xe và AI assistant.
class PromoRepository {
    // Firestore dùng để đọc collection promos do admin cấu hình.
    private val firestore = FirestoreProvider.db

    suspend fun getActivePromos(
        serviceType: String = "bike"
    ): List<Promo> {
        return runCatching {
            firestore.collection("promos")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    val discountType = data["discountType"] as? String
                    val discountValue = (data["discountValue"] as? Number)?.toInt()
                    Promo(
                        code = (data["code"] as? String).orEmpty().ifBlank { document.id },
                        title = (data["title"] as? String).orEmpty(),
                        active = data["active"] as? Boolean ?: true,
                        percent = (data["percent"] as? Number)?.toInt()
                            ?: discountValue?.takeIf { discountType.equals("percent", ignoreCase = true) },
                        amount = (data["amount"] as? Number)?.toInt()
                            ?: discountValue?.takeIf { discountType.equals("fixed", ignoreCase = true) },
                        maxDiscount = (data["maxDiscount"] as? Number)?.toInt(),
                        minPrice = (data["minPrice"] as? Number)?.toInt()
                            ?: (data["minFare"] as? Number)?.toInt(),
                        serviceTypes = (data["serviceTypes"] as? List<*>)
                            ?.mapNotNull { it as? String }
                            .orEmpty(),
                        startAt = (data["startAt"] as? Number)?.toLong() ?: 0L,
                        endAt = (data["endAt"] as? Number)?.toLong() ?: Long.MAX_VALUE
                    )
                }
                .filter { it.code.isNotBlank() && it.isActiveFor(serviceType) }
        }.getOrElse {
            emptyList()
        }
    }

    suspend fun getAvailablePromos(
        serviceType: String = "bike"
    ): List<Promo> {
        return getActivePromos(serviceType)
    }

    // Tính số tiền được giảm dựa trên giá cuốc và loại mã percent/fixed.
    fun calculateDiscount(price: Double, promo: Promo): Double {
        if (promo.minPrice != null && price < promo.minPrice) return 0.0

        return when {
            promo.amount != null -> promo.amount.toDouble()
            promo.percent != null -> {
                val calc = (price * promo.percent) / 100
                promo.maxDiscount?.let { calc.coerceAtMost(it.toDouble()) } ?: calc
            }
            else -> 0.0
        }
    }
}
