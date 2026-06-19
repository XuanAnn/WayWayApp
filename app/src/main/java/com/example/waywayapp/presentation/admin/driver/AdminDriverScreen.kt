package com.example.waywayapp.presentation.admin.driver

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.core.di.AppContainer
import com.example.waywayapp.domain.model.AdminPromo
import com.example.waywayapp.domain.model.AdminDriver
import com.example.waywayapp.domain.model.AdminUser
import com.example.waywayapp.domain.model.Ride
import com.example.waywayapp.presentation.theme.WayWayBackground
import com.example.waywayapp.presentation.theme.WayWayOnSurface
import com.example.waywayapp.presentation.theme.WayWayOnSurfaceVariant
import com.example.waywayapp.presentation.theme.WayWayPrimary
import com.example.waywayapp.presentation.theme.WayWaySurface
import com.example.waywayapp.presentation.theme.WarningOrange

private val AdminBackground = Color(0xFFF7F8FA) // Sáng và dịu hơn
private val AdminSurface = Color.White
private val AdminPrimary = Color(0xFF1DB954) // Xanh thương hiệu WayWay
private val AdminDark = Color(0xFF1A1A1A)
private val AdminMuted = Color(0xFF8A8D93)
private val AdminBorder = Color(0xFFEBECEF)

@Composable
fun AdminDriverScreen(
    viewModel: AdminDriverViewModel = viewModel(
        factory = AdminDriverViewModelFactory(
            AppContainer.provideAdminRepository(),
            promoRepository = AppContainer.provideAdminPromoRepository(),
            rideRepository = AppContainer.provideRideRepository()
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
            when (uiState.selectedSection) {
                AdminSection.USERS,
                AdminSection.DRIVERS,
                AdminSection.PROMOS -> FloatingActionButton(
                    onClick = {
                        when (uiState.selectedSection) {
                            AdminSection.USERS -> viewModel.newUser()
                            AdminSection.DRIVERS -> viewModel.newDriver()
                            AdminSection.PROMOS -> viewModel.newPromo()
                            else -> Unit
                        }
                    },
                    containerColor = AdminPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm mới", modifier = Modifier.size(28.dp))
                }
                AdminSection.RIDES -> Unit
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
            contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardHeader(
                    selectedSection = uiState.selectedSection,
                    count = when (uiState.selectedSection) {
                        AdminSection.USERS -> users.size
                        AdminSection.DRIVERS -> drivers.size
                        AdminSection.PROMOS -> uiState.promos.size
                        AdminSection.RIDES -> uiState.rides.size
                    },
                    onProfileClick = onProfileClick
                )
            }

            item {
                ModernSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange
                )
            }

            item {
                ModernTabs(
                    selectedSection = uiState.selectedSection,
                    onSelectSection = viewModel::selectSection
                )
            }

            when (uiState.selectedSection) {
                AdminSection.USERS -> {
                    if (uiState.isLoadingUsers) item { LoadingRow() }
                    else items(users) { user -> UserRow(user) { viewModel.editUser(user) } }
                }
                AdminSection.DRIVERS -> {
                    if (uiState.isLoadingDrivers) item { LoadingRow() }
                    else items(drivers) { driver -> DriverRow(driver) { viewModel.editDriver(driver) } }
                }
                AdminSection.PROMOS -> {
                    if (uiState.isLoadingPromos) item { LoadingRow() }
                    else items(uiState.promos.filter { it.code.contains(search, true) }) { promo ->
                        PromoRow(promo) { viewModel.editPromo(promo) }
                    }
                }
                AdminSection.RIDES -> {
                    if (uiState.isLoadingRides) item { LoadingRow() }
                    else items(uiState.rides.filter { it.id.contains(search, true) }) { ride ->
                        RideRow(ride) { viewModel.editRide(ride) }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.isUserDetailOpen) {
        ModernFormDialog(title = if (uiState.editingUser.id.isBlank()) "Thêm người dùng" else "Sửa thông tin", onDismiss = viewModel::closeUserDetail) {
            UserEditor(uiState.editingUser, uiState.isSaving, viewModel::onUserNameChange, viewModel::onUserEmailChange, viewModel::onUserPhoneChange, viewModel::onUserRoleChange, viewModel::onUserActiveChange, viewModel::saveUser, { pendingDelete = AdminSection.USERS })
        }
    }

    if (uiState.isDriverDetailOpen) {
        ModernFormDialog(title = if (uiState.editingDriver.id.isBlank()) "Thêm tài xế" else "Sửa tài xế", onDismiss = viewModel::closeDriverDetail) {
            DriverEditor(uiState.editingDriver, uiState.isSaving, viewModel::onDriverNameChange, viewModel::onDriverPhoneChange, viewModel::onDriverEmailChange, viewModel::onVehicleTypeChange, viewModel::onPlateNumberChange, viewModel::onDriverRatingChange, viewModel::onDriverActiveChange, viewModel::onDriverAvailableChange, viewModel::onDriverOnlineChange, viewModel::saveDriver, { pendingDelete = AdminSection.DRIVERS })
        }
    }

    if (uiState.isPromoDetailOpen) {
        ModernFormDialog(title = if (uiState.editingPromo.code.isBlank()) "Khuyến mãi mới" else "Sửa khuyến mãi", onDismiss = viewModel::closePromoDetail) {
            PromoEditor(
                promo = uiState.editingPromo,
                isSaving = uiState.isSaving,
                onCodeChange = viewModel::onPromoCodeChange,
                onTitleChange = viewModel::onPromoTitleChange,
                onActiveChange = viewModel::onPromoActiveChange,
                onDiscountTypeChange = viewModel::onPromoDiscountTypeChange,
                onDiscountValueChange = viewModel::onPromoDiscountValueChange,
                onMinPriceChange = viewModel::onPromoMinPriceChange,
                onMaxDiscountChange = viewModel::onPromoMaxDiscountChange,
                onServiceTypesToggle = viewModel::togglePromoServiceType,
                onSave = viewModel::savePromo,
                onDelete = { pendingDelete = AdminSection.PROMOS }
            ) }
    }

    if (uiState.isRideDetailOpen) {
        ModernFormDialog(title = "Chi tiết chuyến đi", onDismiss = viewModel::closeRideDetail) {
            RideEditor(uiState.editingRide, uiState.isSaving, viewModel::onRideStatusChange, viewModel::onRidePaymentStatusChange, viewModel::onRidePaymentMethodChange, viewModel::saveRide, { pendingDelete = AdminSection.RIDES })
        }
    }

    pendingDelete?.let { section ->
        DeleteConfirmDialog(section, { pendingDelete = null }) {
            pendingDelete = null
            when (section) {
                AdminSection.USERS -> viewModel.deleteUser()
                AdminSection.DRIVERS -> viewModel.deleteDriver()
                AdminSection.PROMOS -> viewModel.deletePromo()
                AdminSection.RIDES -> viewModel.deleteRide()
            }
        }
    }
}

// ============================================================================
// CÁC COMPONENT GIAO DIỆN MỚI (MODERN UI)
// ============================================================================

@Composable
private fun DashboardHeader(selectedSection: AdminSection, count: Int, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Quản trị hệ thống",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AdminDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AdminPrimary))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Đang quản lý $count bản ghi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AdminMuted
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = AdminSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.size(48.dp).clickable(onClick = onProfileClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AdminPrimary, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ModernSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Tìm kiếm...", color = AdminMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AdminMuted) },
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Black.copy(alpha = 0.04f),
            focusedContainerColor = AdminSurface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = AdminPrimary,
            cursorColor = AdminPrimary
        ),
        singleLine = true
    )
}

@Composable
private fun ModernTabs(selectedSection: AdminSection, onSelectSection: (AdminSection) -> Unit) {
    val tabs = listOf(
        Triple(AdminSection.USERS, "Khách", Icons.Default.Person),
        Triple(AdminSection.DRIVERS, "Tài xế", Icons.Default.TwoWheeler),
        Triple(AdminSection.PROMOS, "Ưu đãi", Icons.Default.LocalOffer),
        Triple(AdminSection.RIDES, "Chuyến", Icons.Default.ReceiptLong)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEach { (section, label, icon) ->
            val isSelected = selectedSection == section
            Surface(
                shape = CircleShape,
                color = if (isSelected) AdminPrimary else Color.White,
                border = if (isSelected) null else BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.clickable { onSelectSection(section) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else AdminMuted, modifier = Modifier.size(18.dp))
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else AdminDark
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminListRow(title: String, subtitle: String, meta: String, statusColor: Color, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AdminSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, AdminBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AdminDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 13.sp, color = AdminMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.08f)) {
                Text(
                    text = meta,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Tái thiết kế Form nhập liệu dạng Modal
@Composable
private fun ModernFormDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = AdminSurface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AdminDark)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = AdminMuted)
                    }
                }
                // Content
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// Áp dụng phong cách mới cho các field trong Form
@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null, tint = AdminPrimary) },
        label = { Text(label, color = AdminMuted) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AdminBorder, focusedBorderColor = AdminPrimary, focusedLabelColor = AdminPrimary
        )
    )
}

@Composable
private fun StatusSwitch(checked: Boolean, label: String, onCheckedChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AdminBorder), color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AdminDark)
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AdminPrimary))
        }
    }
}

@Composable
private fun EditorActions(canDelete: Boolean, isSaving: Boolean, onSave: () -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
        Button(
            onClick = onSave, enabled = !isSaving, modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminPrimary)
        ) {
            if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Lưu thay đổi", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        if (canDelete) {
            TextButton(
                onClick = onDelete, enabled = !isSaving, modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Xóa bản ghi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun AdminBottomBar(selectedSection: AdminSection, onSelectSection: (AdminSection) -> Unit) {
    Surface(shadowElevation = 16.dp) {
        NavigationBar(containerColor = AdminSurface, modifier = Modifier.height(72.dp)) {
            val items = listOf(AdminSection.USERS to Icons.Default.Group, AdminSection.DRIVERS to Icons.Default.TwoWheeler, AdminSection.PROMOS to Icons.Default.LocalOffer, AdminSection.RIDES to Icons.Default.ReceiptLong)
            val labels = listOf("Khách", "Tài xế", "Ưu đãi", "Chuyến")

            items.forEachIndexed { index, (section, icon) ->
                NavigationBarItem(
                    selected = selectedSection == section,
                    onClick = { onSelectSection(section) },
                    icon = { Icon(icon, contentDescription = null) },
                    label = { Text(labels[index], fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AdminPrimary, selectedTextColor = AdminPrimary, indicatorColor = AdminPrimary.copy(alpha = 0.1f), unselectedIconColor = AdminMuted, unselectedTextColor = AdminMuted)
                )
            }
        }
    }
}

// CÁC THÀNH PHẦN LOGIC GIỮ NGUYÊN BÊN DƯỚI (Lược bỏ bớt rườm rà)
@Composable private fun UserRow(user: AdminUser, onClick: () -> Unit) = AdminListRow(user.name.ifBlank { "Khách mới" }, user.phone.ifBlank { user.email }, user.role, if (user.isActive) AdminPrimary else AdminMuted, Icons.Default.Person, onClick)
@Composable private fun DriverRow(driver: AdminDriver, onClick: () -> Unit) = AdminListRow(driver.name.ifBlank { "Tài xế mới" }, "${driver.vehicleType.uppercase()} • ${driver.plateNumber}", if (driver.isActive) "H.Động" else "Khóa", if (driver.isActive) AdminPrimary else WarningOrange, Icons.Default.DirectionsCar, onClick)
@Composable private fun PromoRow(promo: AdminPromo, onClick: () -> Unit) = AdminListRow(promo.code, promo.title, if (promo.active) "Hiệu lực" else "Hết hạn", if (promo.active) AdminPrimary else AdminMuted, Icons.Default.LocalOffer, onClick)
@Composable private fun RideRow(ride: Ride, onClick: () -> Unit) = AdminListRow("Tuyến đường", ride.serviceType.uppercase(), ride.status, AdminPrimary, Icons.Default.ReceiptLong, onClick)
@Composable private fun LoadingRow() = Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.Center) { CircularProgressIndicator(color = AdminPrimary) }
@Composable private fun DeleteConfirmDialog(section: AdminSection, onDismiss: () -> Unit, onConfirm: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) }, text = { Text("Dữ liệu sẽ bị xóa vĩnh viễn khỏi hệ thống Firestore.") }, confirmButton = { TextButton(onClick = onConfirm) { Text("Xóa ngay", color = Color(0xFFE53935)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = AdminMuted) } }, containerColor = AdminSurface) }
@Composable private fun DetailInfo(label: String, value: String) { Column(Modifier.padding(vertical = 4.dp)) { Text(label, fontSize = 12.sp, color = AdminMuted); Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AdminDark) } }

// Form Editors (Logic không đổi)
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
    DetailInfo(label = "Mã hồ sơ", value = user.id.ifBlank { "Hệ thống tự động tạo ID sau khi lưu" })

    AdminTextField(user.name, onNameChange, "Họ và tên", Icons.Default.Person)
    AdminTextField(user.email, onEmailChange, "Địa chỉ Email", Icons.Default.Email)
    AdminTextField(user.phone, onPhoneChange, "Số điện thoại", Icons.Default.Phone)

    // Formatted Type Picker
    ChipPicker(
        label = "Quyền hạn hệ thống",
        options = listOf("USER" to "Người dùng", "DRIVER" to "Tài xế", "ADMIN" to "Quản trị viên"),
        selectedId = user.role.uppercase(),
        onSelected = onRoleChange
    )

    StatusSwitch(
        checked = user.isActive,
        label = if (user.isActive) "Tài khoản đang hoạt động (Hiệu lực)" else "Tài khoản đã bị khóa",
        onCheckedChange = onActiveChange
    )

    EditorActions(canDelete = user.id.isNotBlank(), isSaving = isSaving, onSave = onSave, onDelete = onDelete)
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
    DetailInfo(label = "Mã tài xế (UID)", value = driver.id.ifBlank { "Tạo tự động" })

    AdminTextField(driver.name, onNameChange, "Họ tên tài xế", Icons.Default.Badge)
    AdminTextField(driver.phone, onPhoneChange, "Số điện thoại liên hệ", Icons.Default.Phone)
    AdminTextField(driver.plateNumber, onPlateNumberChange, "Biển kiểm soát", Icons.Default.DirectionsCar)
    AdminTextField(driver.rating.toString(), onRatingChange, "Điểm đánh giá (0.0 - 5.0)", Icons.Default.Badge)

    ChipPicker(
        label = "Loại phương tiện cung cấp",
        options = SystemServiceType.values().map { it.id to it.title },
        selectedId = driver.vehicleType,
        onSelected = onVehicleTypeChange
    )

    StatusSwitch(checked = driver.isActive, label = "Hồ sơ tài xế hợp lệ", onCheckedChange = onActiveChange)
    StatusSwitch(checked = driver.isOnline, label = "Trạng thái ứng dụng (Online)", onCheckedChange = onOnlineChange)
    StatusSwitch(checked = driver.isAvailable, label = "Đang sẵn sàng nhận chuyến", onCheckedChange = onAvailableChange)

    EditorActions(canDelete = driver.id.isNotBlank(), isSaving = isSaving, onSave = onSave, onDelete = onDelete)
}

@Composable
private fun PromoEditor(
    promo: AdminPromo,
    isSaving: Boolean,
    onCodeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onDiscountTypeChange: (String) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxDiscountChange: (String) -> Unit,
    onServiceTypesToggle: (String) -> Unit, // Đã đổi callback để dùng Checkbox
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    AdminTextField(promo.code, onCodeChange, "Mã khuyến mãi (Viết liền không dấu)", Icons.Default.LocalOffer)
    AdminTextField(promo.title, onTitleChange, "Tên chương trình", Icons.Default.ListAlt)

    ChipPicker(
        label = "Loại hình giảm giá",
        options = DiscountType.values().map { it.id to it.title },
        selectedId = promo.discountType,
        onSelected = onDiscountTypeChange
    )

    AdminTextField(promo.discountValue.toString(), onDiscountValueChange, "Giá trị giảm (VNĐ hoặc %)", Icons.Default.Badge)
    AdminTextField((promo.minPrice ?: 0).toString(), onMinPriceChange, "Đơn tối thiểu để áp dụng (VNĐ)", Icons.Default.Badge)

    if (promo.discountType == DiscountType.PERCENT.id) {
        AdminTextField((promo.maxDiscount ?: 0).toString(), onMaxDiscountChange, "Mức giảm tối đa cho phép (VNĐ)", Icons.Default.Badge)
    }

    Text("Áp dụng cho dịch vụ:", style = MaterialTheme.typography.labelMedium, color = AdminMuted, modifier = Modifier.padding(top = 8.dp))
    ServiceTypeSelector(
        selectedServices = promo.serviceTypes,
        onServiceToggle = onServiceTypesToggle
    )

    StatusSwitch(checked = promo.active, label = "Kích hoạt khuyến mãi", onCheckedChange = onActiveChange)

    EditorActions(canDelete = promo.code.isNotBlank(), isSaving = isSaving, onSave = onSave, onDelete = onDelete)
}

// Cập nhật lại ChipPicker để nhận danh sách Pair (ID, Hiển thị) thay vì List String đơn thuần
@Composable
private fun ChipPicker(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AdminMuted)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (id, displayTitle) ->
                val isSelected = selectedId.equals(id, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AdminPrimary.copy(alpha = 0.1f) else AdminSurface,
                    border = BorderStroke(1.dp, if (isSelected) AdminPrimary else AdminBorder),
                    modifier = Modifier.clickable { onSelected(id) }
                ) {
                    Text(
                        text = displayTitle,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AdminPrimary else AdminDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun ServiceTypeSelector(
    selectedServices: List<String>,
    onServiceToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SystemServiceType.values().forEach { service ->
            val isChecked = selectedServices.contains(service.id)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isChecked) AdminPrimary.copy(alpha = 0.05f) else Color.Transparent,
                border = BorderStroke(1.dp, if (isChecked) AdminPrimary else AdminBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onServiceToggle(service.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = null // Sự kiện click đã được xử lý ở Surface bên ngoài
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = service.title,
                        fontSize = 14.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChecked) AdminPrimary else AdminDark
                    )
                }
            }
        }
    }
}
@Composable private fun PromoEditor(promo: AdminPromo, isSaving: Boolean, onCodeChange: (String) -> Unit, onTitleChange: (String) -> Unit, onActiveChange: (Boolean) -> Unit, onDiscountTypeChange: (String) -> Unit, onDiscountValueChange: (String) -> Unit, onMinPriceChange: (String) -> Unit, onMaxDiscountChange: (String) -> Unit, onServiceTypesChange: (String) -> Unit, onStartAtChange: (String) -> Unit, onEndAtChange: (String) -> Unit, onSave: () -> Unit, onDelete: () -> Unit) { AdminTextField(promo.code, onCodeChange, "Mã Code", Icons.Default.LocalOffer); AdminTextField(promo.title, onTitleChange, "Tiêu đề", Icons.Default.ListAlt); StatusSwitch(promo.active, "Đang kích hoạt", onActiveChange); EditorActions(promo.code.isNotBlank(), isSaving, onSave, onDelete) }
@Composable private fun RideEditor(ride: Ride, isSaving: Boolean, onStatusChange: (String) -> Unit, onPaymentStatusChange: (String) -> Unit, onPaymentMethodChange: (String) -> Unit, onSave: () -> Unit, onDelete: () -> Unit) { AdminTextField(ride.status, onStatusChange, "Trạng thái chuyến", Icons.Default.ReceiptLong); EditorActions(ride.id.isNotBlank(), isSaving, onSave, onDelete) }