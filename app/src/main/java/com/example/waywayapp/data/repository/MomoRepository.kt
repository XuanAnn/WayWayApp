package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Repository liên kết/hủy liên kết ví MoMo UAT trong hồ sơ người dùng.
class MomoRepository {
    // Firebase Auth lấy uid user đang liên kết ví.
    private val auth = Firebase.auth
    // Firestore lưu trạng thái ví ở users và log riêng ở momoLinks.
    private val firestore = FirestoreProvider.db

    // Lưu số điện thoại MoMo UAT vào users/{uid} và momoLinks/{uid}.
    suspend fun linkMomoUat(
        phone: String
    ) {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Bạn cần đăng nhập trước khi liên kết MoMo."
        }
        val normalizedPhone = normalizePhone(phone.ifBlank { auth.currentUser?.phoneNumber.orEmpty() })
        if (normalizedPhone.isBlank()) {
            throw IllegalArgumentException("Vui lòng nhập số điện thoại MoMo.")
        }

        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "momoLinked" to true,
                    "momoPhone" to normalizedPhone,
                    "momoLinkedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()

        firestore.collection("momoLinks")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "phone" to normalizedPhone,
                    "environment" to "UAT",
                    "provider" to "MoMo",
                    "status" to "LINKED",
                    "linkedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    // Hủy liên kết MoMo bằng cách cập nhật trạng thái trong Firestore.
    suspend fun unlinkMomo() {
        val uid = requireNotNull(auth.currentUser?.uid) {
            "Bạn cần đăng nhập trước."
        }
        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "momoLinked" to false,
                    "momoPhone" to "",
                    "momoLinkedAt" to 0L,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()

        firestore.collection("momoLinks")
            .document(uid)
            .set(
                mapOf(
                    "status" to "UNLINKED",
                    "unlinkedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    // Chuẩn hoá số điện thoại ví để lưu thống nhất dạng +84.
    private fun normalizePhone(value: String): String {
        val raw = value.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
        return when {
            raw.isBlank() -> ""
            raw.startsWith("+") -> raw
            raw.startsWith("0") -> "+84${raw.drop(1)}"
            else -> raw
        }
    }
}
