package com.example.waywayapp.ui.user.booking.bike.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel

@Composable
fun BikeSearchScreen(
    onBackClick: () -> Unit = {},
    onPickupMapClick: () -> Unit = {},
    onDropoffMapClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: BikeViewModel = BikeSharedViewModel.viewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AddressInputRow(
                        iconColor = Color(0xFF1E73E8),
                        value = uiState.pickupAddress,
                        placeholder = "Vị trí hiện tại",
                        onClick = onPickupMapClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.dropoffAddress,
                        onValueChange = { value ->
                            viewModel.onSearchQueryChange(value)
                        },
                        placeholder = {
                            Text("Bạn muốn đến đâu?")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFE53935)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onDropoffMapClick) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = onDropoffMapClick) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Dùng gần đây") }
                )

                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Đề xuất") }
                )

                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Đã lưu") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                if (searchResults.isNotEmpty()) {
                    items(searchResults) { result ->
                        PlaceItem(
                            title = result.display_name.take(35),
                            subtitle = result.display_name,
                            onClick = {
                                viewModel.searchLocation(result.display_name)
                                viewModel.clearSearchResults()
                                onConfirmClick()
                            }
                        )
                    }
                } else {
                    val recentPlaces = listOf(
                        "410 Trần Đại Nghĩa",
                        "57 Phan Văn Trị",
                        "69/17 Nguyễn Chí Thanh",
                        "45 Phan Văn Trị",
                        "Chùa Quan Minh"
                    )

                    items(recentPlaces) { place ->
                        PlaceItem(
                            title = place,
                            subtitle = "Đà Nẵng, Việt Nam",
                            onClick = {
                                viewModel.onSearchQueryChange(place)
                                viewModel.searchLocation(place)
                                onConfirmClick()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDropoffMapClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEAF8F4),
                            contentColor = Color(0xFF0B5D50)
                        )
                    ) {
                        Text("Chọn trên bản đồ")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressInputRow(
    iconColor: Color,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MyLocation,
            contentDescription = null,
            tint = iconColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value.ifBlank { placeholder },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun PlaceItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF8F4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF0B5D50)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF20242A)
            )

            Text(
                text = subtitle,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
}