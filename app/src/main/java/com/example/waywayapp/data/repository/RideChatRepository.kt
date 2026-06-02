package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.RideMessage
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.example.waywayapp.data.remote.dto.firestore.toRideMessageDto
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repository quản lý tin nhắn giữa user và driver trong từng chuyến xe.
class RideChatRepository {
    // Firestore lưu message dưới rides/{rideId}/messages.
    private val firestore = FirestoreProvider.db
    // Firebase Auth dùng để lấy người gửi hiện tại.
    private val auth = Firebase.auth

    // Lắng nghe realtime danh sách tin nhắn của một chuyến xe.
    fun observeMessages(
        rideId: String
    ): Flow<List<RideMessage>> = callbackFlow {
        val registration = firestore.collection("messages")
            .whereEqualTo("rideId", rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents
                    ?.mapNotNull { document -> document.toRideMessageDto()?.toDomain(document.id) }
                    ?.sortedBy { message -> message.createdAt }
                    .orEmpty()

                trySend(messages)
            }

        awaitClose {
            registration.remove()
        }
    }

    // Gửi tin nhắn mới vào subcollection messages của ride hiện tại.
    suspend fun sendMessage(
        rideId: String,
        text: String
    ) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return

        val user = requireNotNull(auth.currentUser) {
            "Ban can dang nhap de nhan tin."
        }
        val document = firestore.collection("messages").document()
        // Tên người gửi ưu tiên displayName, sau đó email hoặc số điện thoại.
        val senderName = user.displayName
            ?: user.email
            ?: user.phoneNumber
            ?: "Nguoi dung"
        val message = RideMessage(
            id = document.id,
            rideId = rideId,
            senderId = user.uid,
            senderName = senderName,
            text = trimmedText,
            createdAt = System.currentTimeMillis()
        )

        document.set(message.toDto()).await()
    }
}
