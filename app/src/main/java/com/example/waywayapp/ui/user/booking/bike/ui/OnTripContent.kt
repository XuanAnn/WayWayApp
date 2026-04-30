package com.example.waywayapp.ui.user.booking.bike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BikeState

@Composable
fun OnTripUI(state: BikeState, onComplete: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tài xế đang đến",
                    fontSize = 14.sp,
                    color = Color(0xFF00B1A7),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Driver Image Placeholder
                        Box(
                            modifier = Modifier
                                .size(50.dp)
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
                        Text(text = "Honda Vision", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { /* Call */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF00B1A7))
                    }
                    
                    IconButton(
                        onClick = { /* Chat */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF00B1A7))
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(2f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B1A7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hoàn thành")
                    }
                }
            }
        }
    }
}
