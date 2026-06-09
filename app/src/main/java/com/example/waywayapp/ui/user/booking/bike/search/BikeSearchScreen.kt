package com.example.waywayapp.ui.user.booking.bike.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val colors = MaterialTheme.colorScheme

    Scaffold(containerColor = colors.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = colors.onBackground)
                }

                Column(modifier = Modifier.weight(1f)) {
                    AddressInputRow(
                        iconColor = colors.primary,
                        value = uiState.pickupAddress,
                        placeholder = "Vị trí hiện tại",
                        onClick = onPickupMapClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.dropoffAddress,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = { Text("Bạn muốn đến đâu?") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colors.error
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onDropoffMapClick) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Chọn trên bản đồ")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            focusedLabelColor = colors.primary,
                            cursorColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = onDropoffMapClick) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Thêm điểm đến", tint = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text("Dùng gần đây") })
                FilterChip(selected = false, onClick = {}, label = { Text("Đề xuất") })
                FilterChip(selected = false, onClick = {}, label = { Text("Đã lưu") })
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
                            containerColor = colors.primaryContainer,
                            contentColor = colors.onPrimaryContainer
                        )
                    ) {
                        Text("Chọn trên bản đồ", fontWeight = FontWeight.Bold)
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
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.MyLocation, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.ifBlank { placeholder },
            modifier = Modifier.weight(1f),
            color = colors.onSurface,
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
    val colors = MaterialTheme.colorScheme

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
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = colors.onPrimaryContainer)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = colors.onBackground)
            Text(text = subtitle, color = colors.onSurfaceVariant, maxLines = 2)
        }
    }
}
