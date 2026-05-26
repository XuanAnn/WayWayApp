package com.example.waywayapp.ui.user.booking.car.search

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.waywayapp.ui.user.booking.car.CarSharedViewModel
import com.example.waywayapp.ui.user.booking.car.CarViewModel

@Composable
fun CarSearchScreen(
    onBackClick: () -> Unit = {},
    onPickupMapClick: () -> Unit = {},
    onDropoffMapClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: CarViewModel = CarSharedViewModel.viewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && uiState.currentLatLng == null) {
            viewModel.initLocation(context)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            OutlinedTextField(
                value = uiState.pickupAddress,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = onPickupMapClick) {
                        Icon(Icons.Default.Map, contentDescription = null)
                    }
                },
                label = { Text("Điểm đón") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.dropoffAddress,
                onValueChange = viewModel::onSearchQueryChange,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = onDropoffMapClick) {
                        Icon(Icons.Default.Map, contentDescription = null)
                    }
                },
                label = { Text("Bạn muốn đến đâu?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn {
                items(searchResults) { result ->
                    Text(
                        text = result.display_name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.searchLocation(result.display_name)
                                viewModel.clearSearchResults()
                                onConfirmClick()
                            }
                            .padding(vertical = 14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDropoffMapClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Chọn trên bản đồ")
            }
        }
    }
}
