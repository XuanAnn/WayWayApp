package com.example.waywayapp.presentation.user.booking.bike

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.presentation.chat.RideMessagePopupListener
import com.example.waywayapp.presentation.user.booking.bike.components.BookingInputOverlay
import com.example.waywayapp.presentation.user.booking.bike.components.CompletedUI
import com.example.waywayapp.presentation.user.booking.bike.components.MainMapContent
import com.example.waywayapp.presentation.user.booking.bike.components.OnTripUI
import com.example.waywayapp.presentation.user.booking.bike.components.PromoBottomSheet
import com.example.waywayapp.presentation.user.booking.bike.components.WaitingUI
import com.example.waywayapp.presentation.user.booking.bike.model.BookingStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState


private fun openDialer(
    context: Context,
    phone: String
) {
    val normalizedPhone = phone.trim()
    if (normalizedPhone.isBlank()) return

    val intent = Intent(
        Intent.ACTION_DIAL,
        Uri.parse("tel:$normalizedPhone")
    )
    context.startActivity(intent)
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}
