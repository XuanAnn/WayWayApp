package com.example.waywayapp.ui.user.booking.express.picker

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerScreen(
    type: ExpressLocationType,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: ExpressLocationPickerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraPositionState =
        rememberCameraPositionState()

    val isPickup =
        type == ExpressLocationType.PICKUP
    var searchText by remember {
        mutableStateOf("")
    }
    LaunchedEffect(Unit) {
        viewModel.initLocation(context)
    }

    LaunchedEffect(uiState.currentLatLng) {
        uiState.currentLatLng?.let { latLng ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(
                    latLng,
                    18f
                )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ExpressMapContent(
            cameraPositionState = cameraPositionState,
            type = type,
            onCameraIdle = {
                val center =
                    cameraPositionState.position.target

                viewModel.updateSelectedLocation(center)
            }
        )

        LocationTopSearchBar(
            isPickup = isPickup,
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onBackClick = onBackClick
        )
        if (uiState.searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .offset(y = 84.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column {
                    uiState.searchResults.take(5).forEach { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val latLng = LatLng(
                                        result.lat.toDouble(),
                                        result.lon.toDouble()
                                    )

                                    viewModel.selectSearchResult(result)

                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(
                                            latLng,
                                            18f
                                        )
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF00B14F)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = result.display_name,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = if (isPickup) {
                Color(0xFF1E88E5)
            } else {
                Color(0xFFE53935)
            },
            modifier = Modifier
                .align(Alignment.Center)
                .size(72.dp)
                .offset(y = (-36).dp)
        )

        FloatingActionButton(
            onClick = {
                uiState.currentLatLng?.let { current ->
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(
                            current,
                            18f
                        )

                    viewModel.updateSelectedLocation(current)
                }
            },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = Color(0xFF20242A)
            )
        }

        LocationConfirmPanel(
            address = uiState.selectedAddress,
            isPickup = isPickup,
            onConfirmClick = {
                viewModel.confirmLocation(
                    type = type,
                    onDone = onConfirmClick
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LocationTopSearchBar(
    isPickup: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null
            )
        }

        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = if (isPickup) Color(0xFF1E88E5) else Color(0xFFE53935)
        )

        Spacer(modifier = Modifier.width(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = if (isPickup) {
                        "Lấy hàng tại đâu?"
                    } else {
                        "Giao đến đâu?"
                    }
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.DarkGray
        )

        Spacer(modifier = Modifier.width(12.dp))
    }
}
@Composable
private fun LocationConfirmPanel(
    address: String,
    isPickup: Boolean,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = address,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF20242A),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kiểm tra lại vị trí ghim trước khi xác nhận",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B14F)
                )
            ) {
                Text(
                    text = if (isPickup) {
                        "Chọn điểm lấy này"
                    } else {
                        "Chọn điểm đến này"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}