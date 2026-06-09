package com.example.waywayapp.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waywayapp.data.model.AiChatMessage
import com.example.waywayapp.data.repository.AiChatRepository
import com.example.waywayapp.ui.theme.AppBg
import com.example.waywayapp.ui.theme.BgLight
import com.example.waywayapp.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Màn hình chat AI dùng chung cho User và Driver.
fun AiAssistantScreen(
    role: String,
    onBackClick: () -> Unit,
    repository: AiChatRepository = AiChatRepository()
) {
    // Role quyết định lời chào, câu hỏi gợi ý và context gửi lên backend.
    val normalizedRole = role.uppercase(Locale.ROOT).ifBlank { "USER" }

    val messages = remember(normalizedRole) {
        mutableStateListOf(
            AiChatMessage(
                text = welcomeMessage(normalizedRole),
                isUser = false,
                mode = "local"
            )
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    // Cờ loading để khóa nút gửi khi backend đang xử lý.
    var isSending by remember { mutableStateOf(false) }
    // Lưu lỗi kết nối backend để hiển thị popup trên màn hình.
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size) {
        // Tự cuộn xuống tin nhắn mới nhất sau mỗi lần gửi/nhận.
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (normalizedRole == "DRIVER") "Tro ly tai xe" else "WayWay AI",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgLight)
            )
        },
        bottomBar = {
            AiInputBar(
                value = input,
                isSending = isSending,
                onValueChange = {
                    input = it
                    error = null
                },
                onSend = {
                    val text = input.trim()
                    if (text.isBlank() || isSending) return@AiInputBar
                    // Thêm tin nhắn user vào UI trước để phản hồi nhanh.
                    input = ""
                    messages.add(AiChatMessage(text = text, isUser = true))
                    isSending = true

                    scope.launch {
                        runCatching {
                            // Gửi câu hỏi sang AiChatRepository để gọi backend /api/ai/chat.
                            repository.send(text, normalizedRole)
                        }.onSuccess { (reply, mode) ->
                            messages.add(
                                AiChatMessage(
                                    text = reply.ifBlank { "Minh chua co cau tra loi luc nay." },
                                    isUser = false,
                                    mode = mode
                                )
                            )
                        }.onFailure { throwable ->
                            error = throwable.localizedMessage ?: "Khong ket noi duoc AI assistant."
                            messages.add(
                                AiChatMessage(
                                    text = "Minh dang mat ket noi voi backend. Hay kiem tra server/ngrok roi thu lai.",
                                    isUser = false,
                                    mode = "error"
                                )
                            )
                        }
                        isSending = false
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SuggestedQuestions(
                        role = normalizedRole,
                        onPick = { question ->
                            input = question
                        }
                    )
                }
                items(messages, key = { it.id }) { message ->
                    AiMessageBubble(message = message)
                }
                if (isSending) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    )
                                    Text("Đang suy nghĩ...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
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
private fun SuggestedQuestions(
    role: String,
    onPick: (String) -> Unit
) {
    // Gợi ý khác nhau theo vai trò user hoặc driver.
    val questions = if (role == "DRIVER") {
        listOf(
            "Hom nay toi da kiem duoc bao nhieu?",
            "Lam sao de nhan nhieu cuoc hon?",
            "Khi khach khong nghe may thi lam gi?",
            "Vi tai xe cong tien khi nao?"
        )
    } else {
        listOf(
            "Hom nay co ma giam gia nao khong?",
            "Gia cuoc duoc tinh nhu the nao?",
            "Lam sao theo doi vi tri tai xe?",
            "Toi muon xem lai lich su cuoc"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cau hoi goi y",
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        questions.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { question ->
                    AssistChip(
                        onClick = { onPick(question) },
                        label = { Text(question, maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiChatMessage
) {
    // Định dạng giờ gửi tin nhắn ở dưới bubble.
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!message.isUser) {
                    AiAvatar()
                }
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (message.isUser) 20.dp else 6.dp,
                        bottomEnd = if (message.isUser) 6.dp else 20.dp
                    ),
                    color = if (message.isUser) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color.White,
                    shadowElevation = if (message.isUser) 0.dp else 2.dp
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = if (message.isUser) Color.White else TextDark
                    )
                }
            }
            Text(
                text = buildString {
                    append(timeFormatter.format(Date(message.createdAt)))
                    if (!message.isUser && message.mode.isNotBlank() && message.mode != "openai") {
                        append(" · ")
                        append(message.mode)
                    }
                },
                modifier = Modifier.padding(top = 4.dp, start = 46.dp, end = 6.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun AiAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(androidx.compose.material3.MaterialTheme.colorScheme.primary, androidx.compose.material3.MaterialTheme.colorScheme.primary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun AiInputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    // Thanh nhập tin nhắn nằm dưới cùng và tránh bàn phím bằng imePadding.
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
                placeholder = { Text("Hoi WayWay AI...") },
                maxLines = 4,
                shape = RoundedCornerShape(18.dp)
            )
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .background(
                        if (value.isNotBlank() && !isSending) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
            }
        }
    }
}

private fun welcomeMessage(
    role: String
): String {
    // Lời chào đầu màn hình thay đổi theo vai trò đang sử dụng.
    return if (role == "DRIVER") {
        "Chao ban, minh la tro ly tai xe WayWay. Minh co the ho tro ve nhan cuoc, trang thai chuyen, vi, thu nhap va cach xu ly tinh huong voi khach."
    } else {
        "Chao ban, minh la WayWay AI. Minh co the ho tro ma giam gia, gia cuoc, dat xe, thanh toan, theo doi tai xe va lich su chuyen di."
    }
}
