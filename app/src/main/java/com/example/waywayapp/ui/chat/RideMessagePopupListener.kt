package com.example.waywayapp.ui.chat

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.waywayapp.data.model.RideMessage
import com.example.waywayapp.data.repository.RideChatRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun RideMessagePopupListener(
    rideId: String?,
    onOpenChat: (String) -> Unit,
    enabled: Boolean = true
) {
    val repository = remember { RideChatRepository() }
    val currentUserId = Firebase.auth.currentUser?.uid.orEmpty()
    var pendingMessage by remember(rideId) { mutableStateOf<RideMessage?>(null) }
    var firstSnapshotSeen by remember(rideId) { mutableStateOf(false) }
    var latestSeenCreatedAt by remember(rideId) { mutableStateOf(0L) }

    LaunchedEffect(rideId, enabled, currentUserId) {
        val activeRideId = rideId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (!enabled || currentUserId.isBlank()) return@LaunchedEffect

        repository.observeMessages(activeRideId).collect { messages ->
            val newestMessage = messages.maxByOrNull { it.createdAt }
            if (!firstSnapshotSeen) {
                latestSeenCreatedAt = newestMessage?.createdAt ?: 0L
                firstSnapshotSeen = true
                return@collect
            }

            val incomingMessage = messages
                .filter { it.senderId != currentUserId }
                .filter { it.createdAt > latestSeenCreatedAt }
                .maxByOrNull { it.createdAt }

            if (newestMessage != null) {
                latestSeenCreatedAt = maxOf(latestSeenCreatedAt, newestMessage.createdAt)
            }

            if (incomingMessage != null) {
                pendingMessage = incomingMessage
            }
        }
    }

    pendingMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                pendingMessage = null
            },
            title = {
                Text(message.senderName.ifBlank { "Tin nhắn mới" })
            },
            text = {
                Text(message.text)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingMessage = null
                        rideId?.takeIf { it.isNotBlank() }?.let(onOpenChat)
                    }
                ) {
                    Text("Mở chat")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingMessage = null
                    }
                ) {
                    Text("Đóng")
                }
            }
        )
    }
}
