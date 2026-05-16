package com.example.waywayapp.ui.user.booking.bike.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.waywayapp.ui.theme.BgLight
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.DarkCard
import com.example.waywayapp.ui.theme.Lime
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.viewmodel.BookingStatus
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.DecimalFormat

@Composable
fun MainMapContent(
    viewModel: BikeViewModel,
    cameraPositionState: CameraPositionState
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý quyền truy cập vị trí
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.initLocationClient(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Google Map nền
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            if (uiState.status == BookingStatus.IDLE) {
                viewModel.setDropoffLocation(latLng, "Vị trí đã chọn")
            }
        },
        properties = MapProperties(isMyLocationEnabled = uiState.currentLatLng != null),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = uiState.status == BookingStatus.IDLE,
            zoomControlsEnabled = false
        )
    ) {
        // Điểm đón
        uiState.pickupLatLng?.let {
            Marker(state = MarkerState(position = it), title = "Điểm đón", snippet = uiState.pickupAddress)
        }

        // Điểm đến
        uiState.dropoffLatLng?.let {
            Marker(state = MarkerState(position = it), title = "Điểm đến", snippet = uiState.dropoffAddress)
        }

        // Đường kẻ lộ trình
        if (uiState.polylinePoints.isNotEmpty()) {
            Polyline(points = uiState.polylinePoints, color = Color(0xFF00B1A7), width = 12f)
        }
    }
}
@Composable
fun BookingInputOverlay(
    viewModel: BikeViewModel,
    onConfirmBooking: () -> Unit,
    onSelectPromo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()


    Box(modifier = Modifier.fillMaxSize()) {

        // TOP SEARCH CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BgLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.bike_icon),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Đặt xe máy",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Text(
                            text = "Chọn điểm đón và điểm đến",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LocationInputField(
                    value = uiState.pickupAddress,
                    onValueChange = { viewModel.onPickupAddressChange(it) },
                    label = "Điểm đón",
                    placeholder = "Vị trí hiện tại",
                    iconColor = Lime,
                    icon = Icons.Default.MyLocation
                )

                Spacer(modifier = Modifier.height(10.dp))

                LocationInputField(
                    value = uiState.dropoffAddress,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = "Điểm đến",
                    placeholder = "Bạn muốn đi đâu?",
                    iconColor = Color(0xFFFF5252),
                    icon = Icons.Default.Place
                )

                if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BgLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.heightIn(max = 230.dp)
                        ) {
                            searchResults.forEach { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val latLng = LatLng(
                                                result.lat.toDouble(),
                                                result.lon.toDouble()
                                            )

                                            viewModel.setDropoffLocation(
                                                latLng,
                                                result.display_name
                                            )

                                            viewModel.clearSearchResults()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Lime,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = result.display_name,
                                        fontSize = 13.sp,
                                        color = TextDark,
                                        maxLines = 2,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM BOOKING CARD
        if (uiState.dropoffLatLng != null && !uiState.isLoading && searchResults.isEmpty()) {
            BookingConfirmCard(
                viewModel = viewModel,
                onConfirmBooking = onConfirmBooking,
                onSelectPromo = onSelectPromo,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
@Composable
fun LocationInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    iconColor: Color,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                fontSize = 12.sp
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4CD08D),
            unfocusedBorderColor = Color(0xFFE1E3DC),
            focusedLabelColor = Color(0xFF4CD08D),
            cursorColor = Color(0xFF4CD08D)
        ), keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { /* Handle done action if needed */ }
        )

    )
}

@Composable
fun BookingConfirmCard(
    viewModel: BikeViewModel,
    onConfirmBooking: () -> Unit,
    onSelectPromo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = DecimalFormat("#,###")

    var expanded by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf("Tiền mặt") }

    data class PaymentMethod(val name: String, val icon: Int)

    val payments = listOf(
        PaymentMethod("Tiền mặt", R.drawable.dollar),
        PaymentMethod("Momo", R.drawable.momo_icon),
        PaymentMethod("Thẻ ngân hàng", R.drawable.ic_credit_card)
    )

    val selectedIcon =
        payments.find { it.name == selectedPayment }?.icon ?: R.drawable.dollar

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF5F7F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.bike_icon),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Bike",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF20242A)
                    )

                    Text(
                        text = "${uiState.distance} • ${uiState.duration}",
                        fontSize = 12.sp,
                        color = Color(0xFF8B918A)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatter.format(uiState.finalPrice.toInt())}đ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF20242A)
                    )

                    if (uiState.promoCode != "Áp mã") {
                        Text(
                            text = "${formatter.format(uiState.price.toInt())}đ",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Điểm đến",
                fontSize = 12.sp,
                color = Color(0xFF8B918A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = uiState.dropoffAddress,
                fontSize = 14.sp,
                color = Color(0xFF20242A),
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thanh toán", fontSize = 12.sp, color = Color(0xFF8B918A))

                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF5F7F2))
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(selectedIcon),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = selectedPayment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF20242A),
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            payments.forEach { payment ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(payment.icon),
                                                contentDescription = null,
                                                modifier = Modifier.size(26.dp)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            Text(payment.name)
                                        }
                                    },
                                    onClick = {
                                        selectedPayment = payment.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Ưu đãi", fontSize = 12.sp, color = Color(0xFF8B918A))

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFF4D8))
                            .clickable {
                                viewModel.loadPromos()
                                onSelectPromo()
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.promo),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = uiState.promoCode,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF7A00),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onConfirmBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF20242A),
                    disabledContainerColor = Color(0xFFB6BBB3)
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = uiState.dropoffAddress != "Đang xác định vị trí..." &&
                        uiState.dropoffAddress.isNotBlank()
            ) {
                Text(
                    text = "Xác nhận đặt xe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}