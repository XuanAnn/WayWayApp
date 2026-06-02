package com.example.waywayapp.ui.user.booking.bike.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.bike.BikeState

@Composable
fun OnTripUI(
    state: BikeState,
    cardHeight: Dp,
    onBack: () -> Unit,
    onCallClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.ridePhase.ifBlank { "Tài xế đang đến" },
                    fontSize = 16.sp,
                    color = Color(0xFF00B1A7),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Driver Image Placeholder
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = state.driverName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Text(text = " ${state.driverRating}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = state.driverPlate, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            text = state.driverPhone.ifBlank { "Đang cập nhật SĐT" },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EtaChip(
                        title = "Đến điểm đón",
                        value = state.etaToPickup.ifBlank {
                            if (state.rideStatus == "arrived") "Đã đến" else "Đang tính"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    EtaChip(
                        title = "Đến điểm trả",
                        value = state.etaToDropoff.ifBlank { "Đang tính" },
                        modifier = Modifier.weight(1f)
                    )
                }

                RideStatusBar(
                    rideStatus = state.rideStatus,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF00B1A7))
                    }

                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF00B1A7))
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(2f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tôi chưa sẵn sàng đi!", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RideStatusBar(
    rideStatus: String,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "accepted" to "Đang đến",
        "arrived" to "Đã đến",
        "in_progress" to "Hành trình",
        "completed" to "Hoàn thành"
    )
    val activeIndex = steps.indexOfFirst { it.first == rideStatus }
        .takeIf { it >= 0 }
        ?: 0

    Row(
        modifier = modifier
            .background(Color(0xFFF3F6F4), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFFD1D5DB), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth((activeIndex.toFloat() / (steps.size - 1)).coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(Color(0xFF00B1A7), RoundedCornerShape(2.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, _ ->
                        val reached = index <= activeIndex
                        val current = index == activeIndex
                        val color = if (reached) Color(0xFF00B1A7) else Color(0xFFD1D5DB)

                        Box(
                            modifier = Modifier
                                .size(if (current) 24.dp else 20.dp)
                                .background(color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reached) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, step ->
                    val reached = index <= activeIndex
                    val current = index == activeIndex
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.second,
                            color = if (reached) Color(0xFF0F766E) else Color(0xFF6B7280),
                            fontSize = 10.sp,
                            fontWeight = if (current) FontWeight.ExtraBold else FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.heightIn(min = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EtaChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF3F6F4), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color(0xFF111827),
            fontWeight = FontWeight.ExtraBold
        )
    }
}
