package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.domain.model.AdminPromo
import com.example.waywayapp.domain.model.Promo
import com.example.waywayapp.domain.repository.AdminPromoRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminPromoRepositoryImpl: AdminPromoRepository {
    private val firestore = FirestoreProvider.db

    fun observePromos(): Flow<List<AdminPromo>> = callbackFlow {
        val registration = firestore.collection("promos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val promos = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val data = document.data ?: return@mapNotNull null
                    val discountType = (data["discountType"] as? String).orEmpty()
                    val discountValue = (data["discountValue"] as? Number)?.toInt()
                    AdminPromo(
                        code = (data["code"] as? String).orEmpty().ifBlank { document.id },
                        title = (data["title"] as? String).orEmpty(),
                        active = data["active"] as? Boolean ?: true,
                        discountType = discountType.ifBlank {
                            when {
                                data["amount"] != null -> "fixed"
                                else -> "percent"
                            }
                        },
                        discountValue = discountValue ?: when {
                            (data["amount"] as? Number) != null -> (data["amount"] as Number).toInt()
                            (data["percent"] as? Number) != null -> (data["percent"] as Number).toInt()
                            else -> 0
                        },
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
                        endAt = (data["endAt"] as? Number)?.toLong() ?: Long.MAX_VALUE,
                        updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L,
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                    )
                }

                trySend(promos.sortedBy { it.code })
            }

        awaitClose { registration.remove() }
    }

    suspend fun savePromo(promo: AdminPromo) {
        val now = System.currentTimeMillis()
        val code = promo.code.trim().uppercase()
        require(code.isNotBlank()) { "Mã promo không được để trống" }

        val document = firestore.collection("promos").document(code)
        val saved = promo.copy(
            code = code,
            title = promo.title.trim(),
            active = promo.active,
            discountType = normalizeDiscountType(promo.discountType, promo.amount, promo.percent),
            discountValue = promo.discountValue.takeIf { it > 0 } ?: (promo.amount ?: promo.percent ?: 0),
            percent = when (normalizeDiscountType(promo.discountType, promo.amount, promo.percent)) {
                "percent" -> promo.percent ?: promo.discountValue.takeIf { it > 0 }
                else -> null
            },
            amount = when (normalizeDiscountType(promo.discountType, promo.amount, promo.percent)) {
                "fixed" -> promo.amount ?: promo.discountValue.takeIf { it > 0 }
                else -> null
            },
            serviceTypes = promo.serviceTypes.map { it.trim().lowercase() }.filter { it.isNotBlank() }.distinct(),
            updatedAt = now,
            createdAt = if (promo.createdAt == 0L) now else promo.createdAt
        )

        document.set(
            mapOf(
                "code" to saved.code,
                "title" to saved.title,
                "active" to saved.active,
                "discountType" to saved.discountType,
                "discountValue" to saved.discountValue,
                "percent" to saved.percent,
                "amount" to saved.amount,
                "maxDiscount" to saved.maxDiscount,
                "minPrice" to saved.minPrice,
                "serviceTypes" to saved.serviceTypes,
                "startAt" to saved.startAt,
                "endAt" to saved.endAt,
                "updatedAt" to saved.updatedAt,
                "createdAt" to saved.createdAt
            )
        ).await()
    }

    suspend fun deletePromo(code: String) {
        val normalized = code.trim().uppercase()
        if (normalized.isBlank()) return
        firestore.collection("promos")
            .document(normalized)
            .delete()
            .await()
    }

    private fun normalizeDiscountType(
        discountType: String,
        amount: Int?,
        percent: Int?
    ): String {
        return when {
            discountType.equals("fixed", ignoreCase = true) -> "fixed"
            discountType.equals("percent", ignoreCase = true) -> "percent"
            amount != null -> "fixed"
            percent != null -> "percent"
            else -> "percent"
        }
    }
}

fun AdminPromo.toPromo(): Promo {
    return Promo(
        code = code,
        title = title,
        active = active,
        percent = percent,
        amount = amount,
        maxDiscount = maxDiscount,
        minPrice = minPrice,
        serviceTypes = serviceTypes,
        startAt = startAt,
        endAt = endAt
    )
}
