package com.example.waywayapp.ui.user.payment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.AppBg

private val MomoPink = Color(0xFFD82D8B)
private val MomoDark = Color(0xFF2B2330)
private val MomoMuted = Color(0xFF837889)

@Composable
fun MomoLinkScreen(
    viewModel: MomoLinkViewModel = viewModel(),
    onBackClick: () -> Unit = {}
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

    Scaffold(containerColor = AppBg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MomoDark)
                    }
                    Text(
                        text = "Liên kết MoMo UAT",
                        color = MomoDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MomoPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.momo_icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MoMo UAT Wallet", fontWeight = FontWeight.ExtraBold, color = MomoDark)
                                Text(
                                    text = if (uiState.isLinked) "Đã liên kết" else "Chưa liên kết",
                                    color = if (uiState.isLinked) androidx.compose.material3.MaterialTheme.colorScheme.primary else MomoMuted,
                                    fontSize = 13.sp
                                )
                            }
                            if (uiState.isLinked) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            text = "Môi trường UAT dùng để test luồng liên kết. Dữ liệu được lưu vào Firestore và có thể dùng để chọn MoMo khi thanh toán.",
                            color = MomoMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Số điện thoại MoMo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Button(
                            onClick = viewModel::link,
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MomoPink)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(if (uiState.isLinked) "Cập nhật liên kết UAT" else "Liên kết MoMo UAT")
                        }

                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("momo://app")
                                        )
                                    )
                                }.onFailure { error ->
                                    if (error is ActivityNotFoundException) {
                                        Toast.makeText(context, "Thiết bị chưa cài MoMo UAT/app MoMo.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, error.localizedMessage ?: "Không mở được MoMo.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text("Mở ứng dụng MoMo")
                        }

                        if (uiState.isLinked) {
                            OutlinedButton(
                                onClick = viewModel::unlink,
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Icon(Icons.Default.LinkOff, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Hủy liên kết", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MomoPink)
                }
            }
        }
    }
}
