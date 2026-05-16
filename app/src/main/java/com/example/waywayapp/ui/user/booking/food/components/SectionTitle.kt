package com.example.waywayapp.ui.user.booking.food.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(
    title: String,
    action: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color(0xFF20242A),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = action,
            color = Color(0xFF8B918A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}