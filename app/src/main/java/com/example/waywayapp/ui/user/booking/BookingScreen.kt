package com.example.waywayapp.ui.user.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.common.components.MapCompose
import com.example.waywayapp.ui.theme.GoFoodBg
import com.example.waywayapp.ui.theme.GoFoodGreen
import com.example.waywayapp.ui.theme.GoFoodSurface

@Composable
fun BookingScreen(
    viewModel: BookingViewModel = viewModel(),
    onBookingClick: () -> Unit = {},
    onTrackingClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoFoodBg)
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            MapCompose()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(5.dp)
                .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                .background(GoFoodSurface, shape = RoundedCornerShape(12.dp))
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Bạn muốn đi đâu?") },
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, color = GoFoodBg, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GoFoodBg,
                    unfocusedContainerColor = GoFoodBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.padding(end = 8.dp))

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(24.dp))
                    .background(GoFoodSurface, shape = RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            }
        }

        // 3. LAYER TRÊN: Các nút thao tác (Neo phía dưới)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = GoFoodSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onBookingClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoFoodGreen)
                ) {
                    Text("Đặt xe")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onTrackingClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoFoodGreen)

                ) {
                    Text("Theo dõi")
                }
            }
        }
    }
}