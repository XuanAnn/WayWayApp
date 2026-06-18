package com.example.waywayapp.presentation.admin.driver

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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

private val AdminBackground = WayWayBackground
private val AdminSurface = WayWaySurface
private val AdminPrimary = WayWayPrimary
private val AdminDark = WayWayOnSurface
private val AdminGreen = WayWayPrimary
private val AdminOrange = WarningOrange
private val AdminMuted = WayWayOnSurfaceVariant

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
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Them moi")
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
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DashboardTitle(
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
                AdminSearchAndTabs(
                    query = uiState.searchQuery,
                    selectedSection = uiState.selectedSection,
                    onQueryChange = viewModel::onSearchChange,
                    onSelectSection = viewModel::selectSection
                )
            }

            when (uiState.selectedSection) {
                AdminSection.USERS -> {
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
                }
                AdminSection.DRIVERS -> {
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
                AdminSection.PROMOS -> {
                    if (uiState.isLoadingPromos) {
                        item { LoadingRow() }
                    } else {
                        items(
                            uiState.promos.filter { promo ->
                                search.isBlank() ||
                                    promo.code.contains(search, ignoreCase = true) ||
                                    promo.title.contains(search, ignoreCase = true) ||
                                    promo.serviceTypes.any { it.contains(search, ignoreCase = true) }
                            }
                        ) { promo ->
                            PromoRow(
                                promo = promo,
                                onClick = { viewModel.editPromo(promo) }
                            )
                        }
                    }
                }
                AdminSection.RIDES -> {
                    if (uiState.isLoadingRides) {
                        item { LoadingRow() }
                    } else {
                        items(
                            uiState.rides.filter { ride ->
                                search.isBlank() ||
                                    ride.id.contains(search, ignoreCase = true) ||
                                    ride.userId.contains(search, ignoreCase = true) ||
                                    ride.driverId.contains(search, ignoreCase = true) ||
                                    ride.pickupAddress.contains(search, ignoreCase = true) ||
                                    ride.dropoffAddress.contains(search, ignoreCase = true) ||
                                    ride.status.contains(search, ignoreCase = true)
                            }
                        ) { ride ->
                            RideRow(
                                ride = ride,
                                onClick = { viewModel.editRide(ride) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isUserDetailOpen) {
        AdminDetailDialog(
            title = if (uiState.editingUser.id.isBlank()) "ThÃªm ngÆ°á»i dÃ¹ng" else "Chi tiáº¿t ngÆ°á»i dÃ¹ng",
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
            title = if (uiState.editingDriver.id.isBlank()) "ThÃªm tÃ i xáº¿" else "Chi tiáº¿t tÃ i xáº¿",
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

    if (uiState.isPromoDetailOpen) {
        AdminDetailDialog(
            title = if (uiState.editingPromo.code.isBlank()) "Them promo" else "Chi tiet promo",
            onDismiss = viewModel::closePromoDetail
        ) {
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
                onServiceTypesChange = viewModel::onPromoServiceTypesChange,
                onStartAtChange = viewModel::onPromoStartAtChange,
                onEndAtChange = viewModel::onPromoEndAtChange,
                onSave = viewModel::savePromo,
                onDelete = { pendingDelete = AdminSection.PROMOS }
            )
        }
    }

    if (uiState.isRideDetailOpen) {
        AdminDetailDialog(
            title = if (uiState.editingRide.id.isBlank()) "Ride detail" else "Ride detail",
            onDismiss = viewModel::closeRideDetail
        ) {
            RideEditor(
                ride = uiState.editingRide,
                isSaving = uiState.isSaving,
                onStatusChange = viewModel::onRideStatusChange,
                onPaymentStatusChange = viewModel::onRidePaymentStatusChange,
                onPaymentMethodChange = viewModel::onRidePaymentMethodChange,
                onSave = viewModel::saveRide,
                onDelete = { pendingDelete = AdminSection.RIDES }
            )
        }
    }
    pendingDelete?.let { section ->
        DeleteConfirmDialog(
            section = section,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                pendingDelete = null
                when (section) {
                    AdminSection.USERS -> viewModel.deleteUser()
                    AdminSection.DRIVERS -> viewModel.deleteDriver()
                    AdminSection.PROMOS -> viewModel.deletePromo()
                    AdminSection.RIDES -> viewModel.deleteRide()
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
                text = when (selectedSection) {
                    AdminSection.USERS -> "$count ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng"
                    AdminSection.DRIVERS -> "$count tÃƒÂ i xÃ¡ÂºÂ¿"
                    AdminSection.PROMOS -> "$count promo"
                    AdminSection.RIDES -> "$count chuyÃ¡ÂºÂ¿n"
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
                label = { Text("TÃ¬m kiáº¿m") }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.USERS,
                    onClick = { onSelectSection(AdminSection.USERS) },
                    label = { Text("Users") },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.DRIVERS,
                    onClick = { onSelectSection(AdminSection.DRIVERS) },
                    label = { Text("Drivers") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) }
                )
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.PROMOS,
                    onClick = { onSelectSection(AdminSection.PROMOS) },
                    label = { Text("Promos") },
                    leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) }
                )
                ElevatedFilterChip(
                    selected = selectedSection == AdminSection.RIDES,
                    onClick = { onSelectSection(AdminSection.RIDES) },
                    label = { Text("Rides") },
                    leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
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
                Text("ÄÃ³ng")
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
    val label = when (section) {
        AdminSection.USERS -> "nguoi dung"
        AdminSection.DRIVERS -> "tai xe"
        AdminSection.PROMOS -> "promo"
        AdminSection.RIDES -> "chuyen xe"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("XÃ¡c nháº­n xoÃ¡") },
        text = { Text("Báº¡n cÃ³ cháº¯c muá»‘n xoÃ¡ $label nÃ y khÃ´ng? Thao tÃ¡c nÃ y khÃ´ng thá»ƒ hoÃ n tÃ¡c trong app.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("XoÃ¡", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huá»·")
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
    DetailInfo(label = "MÃ£ há»“ sÆ¡", value = user.id.ifBlank { "Sáº½ táº¡o khi lÆ°u" })
    AdminTextField(user.name, onNameChange, "TÃªn ngÆ°á»i dÃ¹ng", Icons.Default.Person)
    AdminTextField(user.email, onEmailChange, "Email", Icons.Default.Email)
    AdminTextField(user.phone, onPhoneChange, "Sá»‘ Ä‘iá»‡n thoáº¡i", Icons.Default.Phone)

    ChipPicker(
        label = "PhÃ¢n quyá»n",
        options = listOf("USER", "DRIVER", "ADMIN"),
        selected = user.role.uppercase(),
        onSelected = onRoleChange
    )

    StatusSwitch(
        checked = user.isActive,
        label = if (user.isActive) "TÃ i khoáº£n Ä‘ang hoáº¡t Ä‘á»™ng" else "TÃ i khoáº£n bá»‹ khoÃ¡",
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
    DetailInfo(label = "MÃ£ há»“ sÆ¡", value = driver.id.ifBlank { "Sáº½ táº¡o khi lÆ°u" })
    DetailInfo(label = "LiÃªn káº¿t user", value = driver.userId.ifBlank { "Tá»± gÃ¡n theo há»“ sÆ¡" })
    AdminTextField(driver.name, onNameChange, "TÃªn tÃ i xáº¿", Icons.Default.Badge)
    AdminTextField(driver.phone, onPhoneChange, "Sá»‘ Ä‘iá»‡n thoáº¡i", Icons.Default.Phone)
    AdminTextField(driver.email, onEmailChange, "Email", Icons.Default.Email)
    AdminTextField(driver.plateNumber, onPlateNumberChange, "Biá»ƒn sá»‘", Icons.Default.DirectionsCar)
    AdminTextField(driver.rating.toString(), onRatingChange, "ÄÃ¡nh giÃ¡", Icons.Default.Badge)

    ChipPicker(
        label = "Loáº¡i xe",
        options = listOf("bike", "car", "express"),
        selected = driver.vehicleType,
        onSelected = onVehicleTypeChange
    )

    StatusSwitch(
        checked = driver.isActive,
        label = if (driver.isActive) "Há»“ sÆ¡ tÃ i xáº¿ Ä‘ang hoáº¡t Ä‘á»™ng" else "Há»“ sÆ¡ tÃ i xáº¿ bá»‹ khoÃ¡",
        onCheckedChange = onActiveChange
    )
    StatusSwitch(
        checked = driver.isOnline,
        label = if (driver.isOnline) "Äang online" else "Äang offline",
        onCheckedChange = onOnlineChange
    )
    StatusSwitch(
        checked = driver.isAvailable,
        label = if (driver.isAvailable) "Sáºµn sÃ ng nháº­n chuyáº¿n" else "Táº¡m nghá»‰ nháº­n chuyáº¿n",
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
    onServiceTypesChange: (String) -> Unit,
    onStartAtChange: (String) -> Unit,
    onEndAtChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    DetailInfo(label = "Code", value = promo.code.ifBlank { "Will be created on save" })
    AdminTextField(promo.code, onCodeChange, "Promo code", Icons.Default.LocalOffer)
    AdminTextField(promo.title, onTitleChange, "Title", Icons.Default.ReceiptLong)
    AdminTextField(promo.discountType, onDiscountTypeChange, "Discount type", Icons.Default.ListAlt)
    AdminTextField(
        promo.discountValue.takeIf { it > 0 }?.toString().orEmpty(),
        onDiscountValueChange,
        "Discount value",
        Icons.Default.Badge
    )
    AdminTextField(
        promo.minPrice?.toString().orEmpty(),
        onMinPriceChange,
        "Min price",
        Icons.Default.Badge
    )
    AdminTextField(
        promo.maxDiscount?.toString().orEmpty(),
        onMaxDiscountChange,
        "Max discount",
        Icons.Default.Badge
    )
    AdminTextField(
        promo.serviceTypes.joinToString(", "),
        onServiceTypesChange,
        "Services: bike, car, express",
        Icons.Default.ListAlt
    )
    AdminTextField(
        promo.startAt.takeIf { it > 0 }?.toString().orEmpty(),
        onStartAtChange,
        "StartAt millis",
        Icons.Default.Badge
    )
    AdminTextField(
        promo.endAt.takeIf { it < Long.MAX_VALUE }?.toString().orEmpty(),
        onEndAtChange,
        "EndAt millis",
        Icons.Default.Badge
    )

    StatusSwitch(
        checked = promo.active,
        label = if (promo.active) "Active" else "Inactive",
        onCheckedChange = onActiveChange
    )

    EditorActions(
        canDelete = promo.code.isNotBlank(),
        isSaving = isSaving,
        onSave = onSave,
        onDelete = onDelete
    )
}

@Composable
private fun RideEditor(
    ride: Ride,
    isSaving: Boolean,
    onStatusChange: (String) -> Unit,
    onPaymentStatusChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    DetailInfo(label = "Ride ID", value = ride.id.ifBlank { "Will be created from app flow" })
    DetailInfo(label = "User ID", value = ride.userId.ifBlank { "-" })
    DetailInfo(label = "Driver ID", value = ride.driverId.ifBlank { "-" })
    DetailInfo(label = "Price", value = ride.price.toString())
    DetailInfo(label = "Pickup", value = ride.pickupAddress.ifBlank { "-" })
    DetailInfo(label = "Dropoff", value = ride.dropoffAddress.ifBlank { "-" })
    DetailInfo(label = "Service type", value = ride.serviceType)
    AdminTextField(ride.status, onStatusChange, "Status", Icons.Default.Badge)
    AdminTextField(ride.paymentStatus, onPaymentStatusChange, "Payment status", Icons.Default.Badge)
    AdminTextField(ride.paymentMethod, onPaymentMethodChange, "Payment method", Icons.Default.Badge)

    EditorActions(
        canDelete = ride.id.isNotBlank(),
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
            Icon(Icons.Default.Delete, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(6.dp))
            Text("XoÃ¡ há»“ sÆ¡", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        }
    }
}


@Composable
fun ServiceTypeSelector(
    selectedServices: List<String>,
    onServiceToggle: (String) -> Unit
) {
    Column {
        SystemServiceType.values().forEach { service ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedServices.contains(service.id),
                    onCheckedChange = { onServiceToggle(service.id) }
                )
                Text(text = service.title)
            }
        }
    }
}
@Composable
private fun UserRow(
    user: AdminUser,
    onClick: () -> Unit
) {
    AdminListRow(
        title = user.name.ifBlank { "ChÆ°a Ä‘áº·t tÃªn" },
        subtitle = user.email.ifBlank { user.phone },
        meta = "${user.role.uppercase()} â€¢ ${if (user.isActive) "Äang hoáº¡t Ä‘á»™ng" else "ÄÃ£ khoÃ¡"}",
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
        title = driver.name.ifBlank { "ChÆ°a Ä‘áº·t tÃªn" },
        subtitle = "${driver.vehicleType} â€¢ ${driver.plateNumber.ifBlank { "ChÆ°a cÃ³ biá»ƒn sá»‘" }}",
        meta = "${driver.phone} â€¢ ${driverStatusText(driver)}",
        statusColor = if (!driver.isActive) AdminMuted else if (driver.isAvailable) AdminGreen else AdminOrange,
        icon = Icons.Default.TwoWheeler,
        onClick = onClick
    )
}

@Composable
private fun PromoRow(
    promo: AdminPromo,
    onClick: () -> Unit
) {
    val discountText = when {
        promo.discountType.equals("fixed", ignoreCase = true) -> {
            "${promo.amount ?: promo.discountValue} VND"
        }
        promo.percent != null || promo.discountType.equals("percent", ignoreCase = true) -> {
            "${promo.percent ?: promo.discountValue}%"
        }
        else -> "${promo.discountValue}"
    }

    val serviceText = if (promo.serviceTypes.isEmpty()) {
        "All services"
    } else {
        promo.serviceTypes.joinToString(", ") { it.uppercase() }
    }

    AdminListRow(
        title = promo.code.ifBlank { "Untitled promo" },
        subtitle = promo.title.ifBlank { serviceText },
        meta = "$discountText • $serviceText",
        statusColor = if (promo.active) AdminGreen else AdminMuted,
        icon = Icons.Default.LocalOffer,
        onClick = onClick
    )
}

@Composable
private fun RideRow(
    ride: Ride,
    onClick: () -> Unit
) {
    val serviceLabel = ride.serviceType.ifBlank { "bike" }.uppercase()
    val statusLabel = ride.status.ifBlank { "searching" }
    val paymentLabel = ride.paymentStatus.ifBlank { "pending" }

    AdminListRow(
        title = "${ride.pickupAddress.ifBlank { "Pickup" }} -> ${ride.dropoffAddress.ifBlank { "Dropoff" }}",
        subtitle = "$serviceLabel • $paymentLabel",
        meta = "${ride.price} • $statusLabel",
        statusColor = when (statusLabel.lowercase()) {
            "completed" -> AdminGreen
            "cancelled" -> AdminMuted
            "accepted", "arrived", "in_progress" -> AdminPrimary
            else -> AdminOrange
        },
        icon = Icons.Default.ReceiptLong,
        onClick = onClick
    )
}

private fun driverStatusText(driver: AdminDriver): String {
    return when {
        !driver.isActive -> "ÄÃ£ khoÃ¡"
        driver.isAvailable -> "Sáºµn sÃ ng"
        driver.isOnline -> "Äang online"
        else -> "Táº¡m nghá»‰"
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
        NavigationBarItem(
            selected = selectedSection == AdminSection.PROMOS,
            onClick = { onSelectSection(AdminSection.PROMOS) },
            icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
            label = { Text("Promos") }
        )
        NavigationBarItem(
            selected = selectedSection == AdminSection.RIDES,
            onClick = { onSelectSection(AdminSection.RIDES) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text("Rides") }
        )
    }
}


