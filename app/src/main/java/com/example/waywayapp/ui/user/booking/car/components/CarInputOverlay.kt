package com.example.waywayapp.ui.user.booking.car.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.waywayapp.R
import com.example.waywayapp.ui.user.booking.car.CarViewModel
import java.text.DecimalFormat

@Composable
fun CarInputOverlay(
    viewModel: CarViewModel,
    onConfirmBooking: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "WayWay Car",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF20242A)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.pickupAddress,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5)
                        )
                    },
                    label = { Text("Điểm đón") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.dropoffAddress,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFE53935)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.clickable {
                                viewModel.searchLocation(uiState.dropoffAddress)
                            }
                        )
                    },
                    label = { Text("Bạn muốn đến đâu?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true
                )

                if (uiState.distance.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    CarServiceCard(
                        priceText = "${formatter.format(uiState.finalPrice.toInt())}đ",
                        distance = uiState.distance,
                        duration = uiState.duration
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onConfirmBooking,
                    enabled = uiState.dropoffLatLng != null && uiState.price > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF20242A),
                        disabledContainerColor = Color(0xFFB6BBB3)
                    )
                ) {
                    Text(
                        text = "Xác nhận đặt xe ô tô",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CarServiceCard(
    priceText: String,
    distance: String,
    duration: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F2))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.car_icon),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Car 4 chỗ",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF20242A)
                )

                Text(
                    text = "$distance • $duration",
                    color = Color.Gray
                )
            }

            Text(
                text = priceText,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF20242A)
            )
        }
    }
}