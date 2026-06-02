package com.example.waywayapp.ui.user.booking.bike.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.BgLight
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.Lime
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun BookingInputOverlay(
    viewModel: BikeViewModel,
    onConfirmBooking: (String) -> Unit,
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
