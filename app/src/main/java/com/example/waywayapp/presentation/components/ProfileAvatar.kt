package com.example.waywayapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProfileAvatar(
    avatarUrl: String?,
    userName: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 42.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF111111)), // Màu nền tối
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            // Hiển thị fallback nếu ảnh lỗi hoặc URL null
            error = null
        )

        // Chỉ hiện initials nếu ảnh URL bị null hoặc rỗng
        if (avatarUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Initials",
                tint = Color.White,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}