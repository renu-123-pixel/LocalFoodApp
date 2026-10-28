package com.ui.screens.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.data.model.PartnerMenuItem
import com.data.model.RegisteredRestaurant
import com.data.model.RegistrationStatus
import com.navigation.PartnerScreen
import com.ui.theme.BackgroundCream
import com.ui.theme.CardWhite
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.TextDark
import com.ui.theme.TextGray
import com.viewmodel.PartnerViewModel

@Composable
fun PartnerDashboardScreen(viewModel: PartnerViewModel, navController: NavController) {
    val restaurant by viewModel.loggedInRestaurant.collectAsState()
    val message by viewModel.message.collectAsState()
    val showAddDialog by viewModel.showAddItemDialog.collectAsState()

    PartnerDashboardContent(
        restaurant = restaurant,
        snackbarMessage = message,
        showAddDialog = showAddDialog,
        onClearMessage = { viewModel.clearMessage() },
        onHideAddItemDialog = { viewModel.hideAddItemDialog() },
        onShowAddItemDialog = { viewModel.showAddItemDialog() },
        onAddMenuItem = { name, price, desc, cat, isVeg ->
            viewModel.addMenuItem(name, price, desc, cat, isVeg)
        },
        onToggleOpenStatus = { viewModel.toggleOpenStatus() },
        onToggleItemAvailability = { viewModel.toggleItemAvailability(it) },
        onRemoveMenuItem = { viewModel.removeMenuItem(it) },
        onLogout = {
            viewModel.logout()
            navController.navigate(PartnerScreen.Entry.route) {
                popUpTo(0) { inclusive = true }
            }
        },
        onNavigateToLogin = {
            navController.navigate(PartnerScreen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardContent(
    restaurant: RegisteredRestaurant?,
    snackbarMessage: String?,
    showAddDialog: Boolean,
    onClearMessage: () -> Unit,
    onHideAddItemDialog: () -> Unit,
    onShowAddItemDialog: () -> Unit,
    onAddMenuItem: (String, Int, String, String, Boolean) -> Unit,
    onToggleOpenStatus: () -> Unit,
    onToggleItemAvailability: (String) -> Unit,
    onRemoveMenuItem: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val indigo = Color(0xFF3949AB)

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    if (restaurant == null) {
        LaunchedEffect(Unit) {
            onNavigateToLogin()
        }
        return
    }

    val r = restaurant

    if (showAddDialog) {
        AddMenuItemDialog(
            onDismiss = onHideAddItemDialog,
            onAdd = onAddMenuItem
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Header ────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB))),
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Partner Dashboard", fontSize = 13.sp, color = Color.White.copy(0.7f))
                                Text(r.restaurantName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text("👤 ${r.ownerName}", fontSize = 13.sp, color = Color.White.copy(0.85f))
                            }
                            Text(r.emoji, fontSize = 44.sp)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(r.status)
                            if (r.status == RegistrationStatus.APPROVED) {
                                OpenClosedToggle(isOpen = r.isOpen, onClick = onToggleOpenStatus)
                            }
                        }
                    }
                }
            }

            // ── Pending Notice ─────────────────────────────
            if (r.status == RegistrationStatus.PENDING) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🕐", fontSize = 28.sp)
                            Column {
                                Text("Application Under Review", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text("Our team will verify your documents and activate your restaurant within 24-48 hours.", fontSize = 13.sp, color = Color(0xFF7E3800))
                            }
                        }
                    }
                }
            }

            // ── Quick Stats ───────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("📦", "Total Orders", "${r.totalOrders}", Color(0xFFE8EAF6), indigo, Modifier.weight(1f))
                    StatCard("⭐", "Rating", if (r.rating == 0.0) "New" else "${r.rating}", Color(0xFFFFF8E1), Color(0xFFFF8C00), Modifier.weight(1f))
                    StatCard("🍽️", "Menu Items", "${r.menuItems.size}", Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }

            // ── Restaurant Details ─────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = CardWhite,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Restaurant Info", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                        InfoRow(Icons.Default.Store, "Category", r.category)
                        InfoRow(Icons.Default.LocationOn, "Address", "${r.address}, ${r.area}")
                        InfoRow(Icons.Default.Schedule, "Hours", "${r.openingTime} – ${r.closingTime}")
                        InfoRow(Icons.Default.Timer, "Delivery", r.deliveryTime)
                        InfoRow(Icons.Default.CurrencyRupee, "Min Order", "₹${r.minOrder}")
                        InfoRow(Icons.Default.VerifiedUser, "FSSAI", r.fssaiNumber)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Menu Header ───────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Menu Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                    FloatingActionButton(
                        onClick = onShowAddItemDialog,
                        containerColor = indigo,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Menu Items ────────────────────────────────
            if (r.menuItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🍽️", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No menu items yet", color = TextGray)
                            Text("Tap + to add your first item", fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            } else {
                items(r.menuItems) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CardWhite,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (item.isVeg) Color(0xFF4CAF50) else Color(0xFFF44336), CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold, color = TextDark)
                                Text("₹${item.price} • ${item.category}", fontSize = 12.sp, color = TextGray)
                            }
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { onToggleItemAvailability(item.id) },
                                colors = SwitchDefaults.colors(checkedThumbColor = indigo, checkedTrackColor = Color(0xFFE8EAF6))
                            )
                            IconButton(onClick = { onRemoveMenuItem(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // ── Logout ────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(emoji: String, label: String, value: String, bgColor: Color, iconColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = iconColor)
            Text(label, fontSize = 10.sp, color = iconColor.copy(0.7f))
        }
    }
}

@Composable
fun StatusBadge(status: RegistrationStatus) {
    val (color, bg) = when(status) {
        RegistrationStatus.PENDING -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        RegistrationStatus.APPROVED -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        RegistrationStatus.REJECTED -> Color(0xFFC62828) to Color(0xFFFFEBEE)
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(status.emoji, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(status.label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OpenClosedToggle(isOpen: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isOpen) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            if (isOpen) "OPEN" else "CLOSED",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextGray)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = TextGray, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemDialog(onDismiss: () -> Unit, onAdd: (String, Int, String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Main Course") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Menu Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVeg, onCheckedChange = { isVeg = it })
                    Text("Pure Veg", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val p = price.toIntOrNull() ?: 0
                onAdd(name, p, desc, category, isVeg) 
            }) { Text("Add Item") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
