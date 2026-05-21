package com.example.waywayapp.ui.user.booking.express.picker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.CameraPositionState

@Composable
fun ExpressMapContent(
    cameraPositionState: CameraPositionState,
    type: ExpressLocationType,
    onCameraIdle: () -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        )
    )
    LaunchedEffect(cameraPositionState.isMoving) {

        if (!cameraPositionState.isMoving) {

            onCameraIdle()
        }
    }
}