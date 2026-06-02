package com.example.waywayapp.ui.user.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val ProfileBg = Color(0xFFF7F6F2)
private val ProfilePrimary = Color(0xFF7C5CFF)
private val ProfileText = Color(0xFF202124)
private val ProfileMuted = Color(0xFF777B74)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message, uiState.error) {
        val message = uiState.message ?: uiState.error
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(containerColor = ProfileBg) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ProfileHeader(
                    name = uiState.user.name.ifBlank { "WayWay User" },
                    role = uiState.user.role
                )
            }

            item {
                ProfileCard(title = "Thông tin tài khoản") {
                    ProfileTextField(
                        value = uiState.user.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Họ tên",
                        icon = Icons.Default.Person
                    )
                    ProfileTextField(
                        value = uiState.user.email,
                        onValueChange = {},
                        label = "Email",
                        icon = Icons.Default.Email,
                        enabled = false
                    )
                    ProfileTextField(
                        value = uiState.user.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = "Số điện thoại",
                        icon = Icons.Default.Phone
                    )
                    InfoRow("Phân quyền", uiState.user.role.uppercase())
                    InfoRow("Trạng thái", if (uiState.user.isActive) "Đang hoạt động" else "Đã khoá")
                }
            }

            uiState.driver?.let { driver ->
                item {
                    ProfileCard(title = "Thông tin tài xế") {
                        ProfileTextField(
                            value = driver.vehicleType,
                            onValueChange = viewModel::onVehicleTypeChange,
                            label = "Loại xe",
                            icon = Icons.Default.DirectionsCar
                        )
                        ProfileTextField(
                            value = driver.plateNumber,
                            onValueChange = viewModel::onPlateNumberChange,
                            label = "Biển số",
                            icon = Icons.Default.Badge
                        )
                        InfoRow("Sẵn sàng", if (driver.isAvailable) "Có" else "Không")
                        InfoRow("Đánh giá", driver.rating.toString())
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Lưu hồ sơ", fontWeight = FontWeight.Bold)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFD93025))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Đăng xuất", color = Color(0xFFD93025), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    role: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(ProfilePrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Column {
            Text(
                text = name,
                color = ProfileText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = role.uppercase(),
                color = ProfileMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = ProfileText)
            content()
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null) },
        label = { Text(label) }
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ProfileMuted)
        Text(value, color = ProfileText, fontWeight = FontWeight.SemiBold)
    }
}
