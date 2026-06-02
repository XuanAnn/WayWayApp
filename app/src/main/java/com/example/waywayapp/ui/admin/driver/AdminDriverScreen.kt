package com.example.waywayapp.ui.admin.driver

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.core.di.AppContainer
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser

private val AdminBackground = Color(0xFFF7F6F2)
private val AdminSurface = Color.White
private val AdminPrimary = Color(0xFF7C5CFF)
private val AdminDark = Color(0xFF202124)
private val AdminGreen = Color(0xFF20B26B)
private val AdminOrange = Color(0xFFE9A23B)
private val AdminMuted = Color(0xFF7D817A)

@Composable
fun AdminDriverScreen(
    viewModel: AdminDriverViewModel = viewModel(
        factory = AdminDriverViewModelFactory(
            AppContainer.provideAdminRepository()
        )
    ),
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<AdminSection?>(null) }
    val search = uiState.searchQuery.trim()

    val users = uiState.users.filter { user ->
        search.isBlank() ||
            user.name.contains(search, ignoreCase = true) ||
            user.email.contains(search, ignoreCase = true) ||
            user.phone.contains(search, ignoreCase = true) ||
            user.role.contains(search, ignoreCase = true)
    }
    val drivers = uiState.drivers.filter { driver ->
        search.isBlank() ||
            driver.name.contains(search, ignoreCase = true) ||
            driver.phone.contains(search, ignoreCase = true) ||
            driver.email.contains(search, ignoreCase = true) ||
            driver.plateNumber.contains(search, ignoreCase = true) ||
            driver.vehicleType.contains(search, ignoreCase = true)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = AdminBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.selectedSection == AdminSection.USERS) {
                        viewModel.newUser()
                    } else {
                        viewModel.newDriver()
                    }
                },
                containerColor = AdminPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mới")
            }
        },
        bottomBar = {
            AdminBottomBar(
                selectedSection = uiState.selectedSection,
                onSelectSection = viewModel::selectSection
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DashboardTitle(
                    selectedSection = uiState.selectedSection,
                    count = if (uiState.selectedSection == AdminSection.USERS) users.size else drivers.size,
                    onProfileClick = onProfileClick
                )
            }

            item {
                AdminSearchAndTabs(
                    query = uiState.searchQuery,
                    selectedSection = uiState.selectedSection,
                    onQueryChange = viewModel::onSearchChange,
                    onSelectSection = viewModel::selectSection
                )
            }

            if (uiState.selectedSection == AdminSection.USERS) {
                if (uiState.isLoadingUsers) {
                    item { LoadingRow() }
                } else {
                    items(users) { user ->
                        UserRow(
                            user = user,
                            onClick = { viewModel.editUser(user) }
                        )
                    }
                }
            } else {
                if (uiState.isLoadingDrivers) {
                    item { LoadingRow() }
                } else {
                    items(drivers) { driver ->
                        DriverRow(
                            driver = driver,
                            onClick = { viewModel.editDriver(driver) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isUserDetailOpen) {
        AdminDetailDialog(
            title = if (uiState.editingUser.id.isBlank()) "Thêm người dùng" else "Chi tiết người dùng",
            onDismiss = viewModel::closeUserDetail
        ) {
            UserEditor(
                user = uiState.editingUser,
                isSaving = uiState.isSaving,
                onNameChange = viewModel::onUserNameChange,
                onEmailChange = viewModel::onUserEmailChange,
                onPhoneChange = viewModel::onUserPhoneChange,
                onRoleChange = viewModel::onUserRoleChange,
                onActiveChange = viewModel::onUserActiveChange,
                onSave = viewModel::saveUser,
                onDelete = { pendingDelete = AdminSection.USERS }
            )
        }
    }

    if (uiState.isDriverDetailOpen) {
        AdminDetailDialog(
            title = if (uiState.editingDriver.id.isBlank()) "Thêm tài xế" else "Chi tiết tài xế",
            onDismiss = viewModel::closeDriverDetail
        ) {
            DriverEditor(
                driver = uiState.editingDriver,
                isSaving = uiState.isSaving,
                onNameChange = viewModel::onDriverNameChange,
                onPhoneChange = viewModel::onDriverPhoneChange,
                onEmailChange = viewModel::onDriverEmailChange,
                onVehicleTypeChange = viewModel::onVehicleTypeChange,
                onPlateNumberChange = viewModel::onPlateNumberChange,
                onRatingChange = viewModel::onDriverRatingChange,
                onActiveChange = viewModel::onDriverActiveChange,
                onAvailableChange = viewModel::onDriverAvailableChange,
                onOnlineChange = viewModel::onDriverOnlineChange,
                onSave = viewModel::saveDriver,
                onDelete = { pendingDelete = AdminSection.DRIVERS }
            )
        }
    }

    pendingDelete?.let { section ->
        DeleteConfirmDialog(
            section = section,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                pendingDelete = null
                if (section == AdminSection.USERS) {
                    viewModel.deleteUser()
                } else {
                    viewModel.deleteDriver()
                }
            }
        )
    }
}

@Composable
private fun DashboardTitle(
    selectedSection: AdminSection,
    count: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AdminDark
            )
            Text(
                text = if (selectedSection == AdminSection.USERS) {
                    "$count người dùng"
                } else {
                    "$count tài xế"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AdminMuted
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AdminPrimary)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AdminSearchAndTabs(
    query: String,
    selectedSection: AdminSection,
    onQueryChange: (String) -> Unit,
    onSelectSection: (AdminSection) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AdminSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                label = { Text("Tìm kiếm") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.USERS,
                    onClick = { onSelectSection(AdminSection.USERS) },
                    label = { Text("Người dùng") },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.DRIVERS,
                    onClick = { onSelectSection(AdminSection.DRIVERS) },
                    label = { Text("Tài xế") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun AdminDetailDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    section: AdminSection,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val label = if (section == AdminSection.USERS) "người dùng" else "tài xế"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận xoá") },
        text = { Text("Bạn có chắc muốn xoá $label này không? Thao tác này không thể hoàn tác trong app.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xoá", color = Color(0xFFD93025))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ")
            }
        }
    )
}

@Composable
private fun UserEditor(
    user: AdminUser,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    DetailInfo(label = "Mã hồ sơ", value = user.id.ifBlank { "Sẽ tạo khi lưu" })
    AdminTextField(user.name, onNameChange, "Tên người dùng", Icons.Default.Person)
    AdminTextField(user.email, onEmailChange, "Email", Icons.Default.Email)
    AdminTextField(user.phone, onPhoneChange, "Số điện thoại", Icons.Default.Phone)

    ChipPicker(
        label = "Phân quyền",
        options = listOf("USER", "DRIVER", "ADMIN"),
        selected = user.role.uppercase(),
        onSelected = onRoleChange
    )

    StatusSwitch(
        checked = user.isActive,
        label = if (user.isActive) "Tài khoản đang hoạt động" else "Tài khoản bị khoá",
        onCheckedChange = onActiveChange
    )

    EditorActions(
        canDelete = user.id.isNotBlank(),
        isSaving = isSaving,
        onSave = onSave,
        onDelete = onDelete
    )
}

@Composable
private fun DriverEditor(
    driver: AdminDriver,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onPlateNumberChange: (String) -> Unit,
    onRatingChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onOnlineChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    DetailInfo(label = "Mã hồ sơ", value = driver.id.ifBlank { "Sẽ tạo khi lưu" })
    DetailInfo(label = "Liên kết user", value = driver.userId.ifBlank { "Tự gán theo hồ sơ" })
    AdminTextField(driver.name, onNameChange, "Tên tài xế", Icons.Default.Badge)
    AdminTextField(driver.phone, onPhoneChange, "Số điện thoại", Icons.Default.Phone)
    AdminTextField(driver.email, onEmailChange, "Email", Icons.Default.Email)
    AdminTextField(driver.plateNumber, onPlateNumberChange, "Biển số", Icons.Default.DirectionsCar)
    AdminTextField(driver.rating.toString(), onRatingChange, "Đánh giá", Icons.Default.Badge)

    ChipPicker(
        label = "Loại xe",
        options = listOf("bike", "car", "express"),
        selected = driver.vehicleType,
        onSelected = onVehicleTypeChange
    )

    StatusSwitch(
        checked = driver.isActive,
        label = if (driver.isActive) "Hồ sơ tài xế đang hoạt động" else "Hồ sơ tài xế bị khoá",
        onCheckedChange = onActiveChange
    )
    StatusSwitch(
        checked = driver.isOnline,
        label = if (driver.isOnline) "Đang online" else "Đang offline",
        onCheckedChange = onOnlineChange
    )
    StatusSwitch(
        checked = driver.isAvailable,
        label = if (driver.isAvailable) "Sẵn sàng nhận chuyến" else "Tạm nghỉ nhận chuyến",
        onCheckedChange = onAvailableChange
    )

    EditorActions(
        canDelete = driver.id.isNotBlank(),
        isSaving = isSaving,
        onSave = onSave,
        onDelete = onDelete
    )
}

@Composable
private fun DetailInfo(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AdminMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = AdminDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null) },
        label = { Text(label) }
    )
}

@Composable
private fun ChipPicker(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AdminMuted)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                ElevatedFilterChip(
                    selected = selected.equals(option, ignoreCase = true),
                    onClick = { onSelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun StatusSwitch(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EditorActions(
    canDelete: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Button(
        onClick = onSave,
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Lưu thay đổi")
    }

    if (canDelete) {
        OutlinedButton(
            onClick = onDelete,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD93025))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Xoá hồ sơ", color = Color(0xFFD93025))
        }
    }
}

@Composable
private fun UserRow(
    user: AdminUser,
    onClick: () -> Unit
) {
    AdminListRow(
        title = user.name.ifBlank { "Chưa đặt tên" },
        subtitle = user.email.ifBlank { user.phone },
        meta = "${user.role.uppercase()} • ${if (user.isActive) "Đang hoạt động" else "Đã khoá"}",
        statusColor = if (user.isActive) AdminGreen else AdminMuted,
        icon = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
        onClick = onClick
    )
}

@Composable
private fun DriverRow(
    driver: AdminDriver,
    onClick: () -> Unit
) {
    AdminListRow(
        title = driver.name.ifBlank { "Chưa đặt tên" },
        subtitle = "${driver.vehicleType} • ${driver.plateNumber.ifBlank { "Chưa có biển số" }}",
        meta = "${driver.phone} • ${driverStatusText(driver)}",
        statusColor = if (!driver.isActive) AdminMuted else if (driver.isAvailable) AdminGreen else AdminOrange,
        icon = Icons.Default.TwoWheeler,
        onClick = onClick
    )
}

private fun driverStatusText(driver: AdminDriver): String {
    return when {
        !driver.isActive -> "Đã khoá"
        driver.isAvailable -> "Sẵn sàng"
        driver.isOnline -> "Đang online"
        else -> "Tạm nghỉ"
    }
}

@Composable
private fun AdminListRow(
    title: String,
    subtitle: String,
    meta: String,
    statusColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AdminSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AdminMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AdminBottomBar(
    selectedSection: AdminSection,
    onSelectSection: (AdminSection) -> Unit
) {
    NavigationBar(containerColor = AdminSurface) {
        NavigationBarItem(
            selected = selectedSection == AdminSection.USERS,
            onClick = { onSelectSection(AdminSection.USERS) },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Users") }
        )
        NavigationBarItem(
            selected = selectedSection == AdminSection.DRIVERS,
            onClick = { onSelectSection(AdminSection.DRIVERS) },
            icon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) },
            label = { Text("Drivers") }
        )
    }
}
