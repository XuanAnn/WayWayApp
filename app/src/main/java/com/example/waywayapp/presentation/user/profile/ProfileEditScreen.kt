package com.example.waywayapp.presentation.user.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

// --- BẢNG MÀU WAYWAY ---
private val WayWayDarkGreen = Color(0xFF006C4C)
private val WayWayLightGreen = Color(0xFF1DB954)
private val AppBgColor = Color(0xFFF5F7FA)
private val CardBgColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1D1B20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val user = uiState.user
    var avatarBitmap by remember(user.avatarUrl) { mutableStateOf<Bitmap?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        viewModel.onAvatarUrlChange(uri.toString())
    }

    LaunchedEffect(uiState.message, uiState.error) {
        val msg = uiState.message ?: uiState.error
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(user.avatarUrl) {
        avatarBitmap = withContext(Dispatchers.IO) {
            loadEditableAvatarBitmap(context, user.avatarUrl)
        }
    }

    Scaffold(
        containerColor = AppBgColor,
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBgColor),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = WayWayDarkGreen)
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Thẻ Avatar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgColor),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(WayWayLightGreen), contentAlignment = Alignment.Center) {
                                // LỖI 3 ĐÃ FIX Ở ĐÂY: Dùng named arguments (bitmap =, contentScale = ...)
                                avatarBitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: Text(user.name.initialsEditable(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp).clip(CircleShape)
                                    .background(WayWayDarkGreen).border(2.dp, Color.White, CircleShape)
                                    .clickable { avatarPicker.launch(arrayOf("image/*")) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Chạm vào camera để cập nhật ảnh", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }

                // Thẻ Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgColor),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = user.name, onValueChange = viewModel::onNameChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Họ tên") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WayWayDarkGreen, focusedLabelColor = WayWayDarkGreen), shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = user.email, onValueChange = {}, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = false, label = { Text("Email (Không thể đổi)") },
                            colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = AppBgColor, disabledTextColor = Color.Gray), shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = user.phone, onValueChange = viewModel::onPhoneChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Số điện thoại") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WayWayDarkGreen, focusedLabelColor = WayWayDarkGreen), shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f)) // Đẩy nút xuống đáy

                Button(
                    onClick = viewModel::saveProfile, enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = WayWayDarkGreen)
                ) {
                    if (uiState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    else Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Text("Lưu thay đổi", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// LỖI 2 ĐÃ FIX Ở ĐÂY: Bỏ từ khóa "private" để tránh xung đột với các file khác,
// hoặc đổi tên hàm thành `loadEditableAvatarBitmap` và `initialsEditable`
internal fun loadEditableAvatarBitmap(context: Context, value: String): Bitmap? {
    if (value.isBlank()) return null
    return runCatching {
        if (value.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(value))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } else {
            URL(value).openStream().use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    }.getOrNull()
}

internal fun String.initialsEditable(): String {
    return trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "U" }
}