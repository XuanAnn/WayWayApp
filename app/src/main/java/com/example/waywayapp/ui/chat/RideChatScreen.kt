package com.example.waywayapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.waywayapp.data.model.RideMessage
import com.example.waywayapp.data.repository.RideChatRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Màn hình chat realtime giữa user và driver theo rideId.
fun RideChatScreen(
    rideId: String,
    onBackClick: () -> Unit
) {
    // Repository đọc/ghi tin nhắn trong Firestore subcollection messages.
    val repository = remember { RideChatRepository() }
    // UID hiện tại dùng để phân biệt bubble của mình và của đối phương.
    val currentUserId = Firebase.auth.currentUser?.uid.orEmpty()
    // Lắng nghe realtime tin nhắn của chuyến xe hiện tại.
    val messages by produceState<List<RideMessage>?>(initialValue = null, rideId) {
        repository.observeMessages(rideId).collect {
            value = it
        }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Tự cuộn xuống tin nhắn mới nhất khi có message mới.
    LaunchedEffect(messages?.size) {
        val lastIndex = messages.orEmpty().lastIndex
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tin nhắn chuyến xe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = input,
                onValueChange = {
                    input = it
                    error = null
                },
                onSend = {
                    val textToSend = input
                    input = ""
                    scope.launch {
                        // Gửi tin nhắn lên Firestore, lỗi thì trả lại text vào ô nhập.
                        runCatching {
                            repository.sendMessage(rideId, textToSend)
                        }.onFailure { throwable ->
                            input = textToSend
                            error = throwable.message ?: "Không gửi được tin nhắn."
                        }
                    }
                },
                enabled = input.isNotBlank()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F7F5))
                .padding(paddingValues)
        ) {
            when {
                messages == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }

                messages.orEmpty().isEmpty() -> {
                    Text(
                        text = "Chưa có tin nhắn. Hãy gửi lời nhắn đầu tiên.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages.orEmpty(), key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                isMine = message.senderId == currentUserId
                            )
                        }
                    }
                }
            }

            error?.let {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3F0),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
// Thanh nhập tin nhắn ở đáy màn hình, có xử lý bàn phím và navigation bar.
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn") },
                maxLines = 4,
                shape = RoundedCornerShape(18.dp)
            )
            IconButton(
                onClick = onSend,
                enabled = enabled,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .background(
                        if (enabled) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White)
            }
        }
    }
}

@Composable
// Bubble hiển thị một tin nhắn, canh phải nếu là tin của user hiện tại.
private fun MessageBubble(
    message: RideMessage,
    isMine: Boolean
) {
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 18.dp
                ),
                color = if (isMine) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color.White,
                shadowElevation = if (isMine) 0.dp else 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (!isMine) {
                        Text(
                            text = message.senderName.ifBlank { "Đối tác chuyến xe" },
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else Color(0xFF111827)
                    )
                }
            }
            Text(
                text = timeFormatter.format(Date(message.createdAt)),
                modifier = Modifier.padding(top = 4.dp, start = 6.dp, end = 6.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
