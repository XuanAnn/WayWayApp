package com.example.waywayapp.ui.admin.driver

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.core.di.AppContainer
import com.example.waywayapp.data.model.AdminDriver

@Composable
fun AdminDriverScreen(
    viewModel: AdminDriverViewModel = viewModel(
        factory = AdminDriverViewModelFactory(
            AppContainer.provideAdminDriverRepository()
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7F2)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Quản lý tài xế",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverForm(
                driver = uiState.editingDriver,
                onNameChange = viewModel::onNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onEmailChange = viewModel::onEmailChange,
                onVehicleTypeChange = viewModel::onVehicleTypeChange,
                onPlateNumberChange = viewModel::onPlateNumberChange,
                onAvailableChange = viewModel::onAvailableChange,
                onSaveClick = viewModel::saveDriver,
                onNewClick = viewModel::newDriver
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.drivers) { driver ->
                        DriverRow(
                            driver = driver,
                            onClick = {
                                viewModel.editDriver(driver)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverForm(
    driver: AdminDriver,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onPlateNumberChange: (String) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onNewClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = driver.name,
                onValueChange = onNameChange,
                label = { Text("Tên tài xế") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = driver.phone,
                onValueChange = onPhoneChange,
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = driver.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = driver.vehicleType,
                    onValueChange = onVehicleTypeChange,
                    label = { Text("Loại xe") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = driver.plateNumber,
                    onValueChange = onPlateNumberChange,
                    label = { Text("Biển số") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = driver.isAvailable,
                    onCheckedChange = onAvailableChange
                )
                Text("Sẵn sàng nhận chuyến")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lưu")
                }

                Button(
                    onClick = onNewClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tạo mới")
                }
            }
        }
    }
}

@Composable
private fun DriverRow(
    driver: AdminDriver,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = driver.name.ifBlank { "Chưa đặt tên" },
                fontWeight = FontWeight.Bold
            )
            Text("${driver.vehicleType} • ${driver.plateNumber}")
            Text(driver.phone)
            Text(
                text = if (driver.isAvailable) "Đang sẵn sàng" else "Tạm nghỉ",
                color = if (driver.isAvailable) Color(0xFF00A152) else Color.Gray
            )
        }
    }
}
