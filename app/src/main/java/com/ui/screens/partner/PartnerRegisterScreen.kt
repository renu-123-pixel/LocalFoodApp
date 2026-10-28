package com.ui.screens.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.navigation.PartnerScreen
import com.ui.components.AppTextField
import com.ui.theme.BackgroundCream
import com.ui.theme.JashpurBhojanTheme
import com.viewmodel.PartnerViewModel

@Composable
fun PartnerRegisterScreen(
    viewModel: PartnerViewModel,
    navController: NavController
) {
    val step by viewModel.registrationStep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val loggedIn by viewModel.loggedInRestaurant.collectAsState()

    LaunchedEffect(loggedIn) {
        if (loggedIn != null) {
            navController.navigate(PartnerScreen.Dashboard.route) {
                popUpTo(PartnerScreen.Entry.route)
            }
        }
    }

    PartnerRegisterContent(
        step = step,
        isLoading = isLoading,
        snackbarMessage = message,
        onClearMessage = { viewModel.clearMessage() },
        onSetStep = { viewModel.setRegistrationStep(it) },
        onShowMsg = { viewModel.showMsg(it) },
        onRegister = { ownerName, ownerPhone, ownerPassword, restaurantName, category, address, area, openingTime, closingTime, minOrder, deliveryTime, fssaiNumber, gstNumber, emoji ->
            viewModel.registerRestaurant(
                ownerName = ownerName,
                ownerPhone = ownerPhone,
                ownerPassword = ownerPassword,
                restaurantName = restaurantName,
                category = category,
                address = address,
                area = area,
                openingTime = openingTime,
                closingTime = closingTime,
                minOrder = minOrder,
                deliveryTime = deliveryTime,
                fssaiNumber = fssaiNumber,
                gstNumber = gstNumber,
                emoji = emoji
            )
        },
        onBack = {
            if (step > 1) viewModel.setRegistrationStep(step - 1)
            else navController.popBackStack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerRegisterContent(
    step: Int,
    isLoading: Boolean,
    snackbarMessage: String?,
    onClearMessage: () -> Unit,
    onSetStep: (Int) -> Unit,
    onShowMsg: (String) -> Unit,
    onRegister: (String, String, String, String, String, String, String, String, String, Int, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Step 1 fields
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var ownerPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Step 2 fields
    var restaurantName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("Jashpur Nagar") }
    var selectedEmoji by remember { mutableStateOf("🍛") }

    // Step 3 fields
    var openingTime by remember { mutableStateOf("09:00 AM") }
    var closingTime by remember { mutableStateOf("10:00 PM") }
    var minOrder by remember { mutableStateOf("80") }
    var deliveryTime by remember { mutableStateOf("30-45 min") }

    // Step 4 fields
    var fssaiNumber by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    val indigo = Color(0xFF3949AB)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Register Restaurant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding)
        ) {
            // Step Indicator
            StepIndicator(currentStep = step, totalSteps = 4)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (step) {

                    // ── Step 1: Owner Details ──────────────────────
                    1 -> {
                        StepHeader("👤 Owner Details", "Tell us about yourself")
                        Spacer(Modifier.height(20.dp))
                        AppTextField(ownerName, { ownerName = it }, "Owner Name *", "Full name", Icons.Default.Person)
                        Spacer(Modifier.height(12.dp))
                        AppTextField(ownerPhone, { ownerPhone = it }, "Mobile Number *", "10-digit number", Icons.Default.Phone)
                        Spacer(Modifier.height(12.dp))
                        AppTextField(ownerPassword, { ownerPassword = it }, "Password *", "Create password", Icons.Default.Lock, isPassword = true)
                        Spacer(Modifier.height(12.dp))
                        AppTextField(confirmPassword, { confirmPassword = it }, "Confirm Password *", "Re-enter password", Icons.Default.Lock, isPassword = true)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                when {
                                    ownerName.isBlank() || ownerPhone.isBlank() || ownerPassword.isBlank() ->
                                        onShowMsg("Please fill all fields")
                                    ownerPhone.length != 10 ->
                                        onShowMsg("Enter valid 10-digit number")
                                    ownerPassword != confirmPassword ->
                                        onShowMsg("Passwords do not match")
                                    ownerPassword.length < 6 ->
                                        onShowMsg("Password must be 6+ characters")
                                    else -> onSetStep(2)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = indigo)
                        ) { Text("Next →", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }

                    // ── Step 2: Restaurant Info ────────────────────
                    2 -> {
                        StepHeader("🏪 Restaurant Details", "Help customers find your restaurant")
                        Spacer(Modifier.height(20.dp))
                        AppTextField(restaurantName, { restaurantName = it }, "Restaurant Name *", "e.g. Sharma Ji Ka Dhaba", Icons.Default.Store)
                        Spacer(Modifier.height(12.dp))

                        // Category dropdown
                        CategoryDropdown(
                            selected = category,
                            onSelect = { category = it }
                        )
                        Spacer(Modifier.height(12.dp))
                        AppTextField(address, { address = it }, "Street Address *", "e.g. Main Bazar, Station Road", Icons.Default.LocationOn)
                        Spacer(Modifier.height(12.dp))
                        AppTextField(area, { area = it }, "Area *", "e.g. Jashpur Nagar", Icons.Default.Place)
                        Spacer(Modifier.height(12.dp))

                        // Emoji picker
                        EmojiPicker(selected = selectedEmoji, onSelect = { selectedEmoji = it })
                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                when {
                                    restaurantName.isBlank() -> onShowMsg("Enter restaurant name")
                                    category.isBlank() -> onShowMsg("Select a category")
                                    address.isBlank() -> onShowMsg("Enter address")
                                    else -> onSetStep(3)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = indigo)
                        ) { Text("Next →", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }

                    // ── Step 3: Timings & Delivery ─────────────────
                    3 -> {
                        StepHeader("⏰ Timings & Delivery", "Set your operational details")
                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                AppTextField(openingTime, { openingTime = it }, "Opening Time", "09:00 AM", Icons.Default.Schedule)
                            }
                            Column(Modifier.weight(1f)) {
                                AppTextField(closingTime, { closingTime = it }, "Closing Time", "10:00 PM", Icons.Default.Schedule)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                AppTextField(minOrder, { minOrder = it }, "Min Order (₹)", "80", Icons.Default.CurrencyRupee)
                            }
                            Column(Modifier.weight(1f)) {
                                AppTextField(deliveryTime, { deliveryTime = it }, "Delivery Time", "30-45 min", Icons.Default.Timer)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8EAF6)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = indigo, modifier = Modifier.size(18.dp))
                                Text("You can update these anytime from your dashboard", fontSize = 13.sp, color = indigo)
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { onSetStep(4) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = indigo)
                        ) { Text("Next →", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }

                    // ── Step 4: Legal Documents ────────────────────
                    4 -> {
                        StepHeader("📄 Legal Details", "Required for approval")
                        Spacer(Modifier.height(20.dp))

                        AppTextField(fssaiNumber, { fssaiNumber = it }, "FSSAI License Number *", "Food safety license", Icons.Default.VerifiedUser)
                        Spacer(Modifier.height(12.dp))
                        AppTextField(gstNumber, { gstNumber = it }, "GST Number (Optional)", "GST registration number", Icons.Default.Receipt)
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("⚠️ Review Process", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Your restaurant will be reviewed within 24-48 hours. Once approved, it will appear live on the app for customers in Jashpur.",
                                    fontSize = 13.sp, color = Color(0xFF7E3800)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        // Summary Card
                        SummaryCard(
                            emoji = selectedEmoji,
                            name = restaurantName,
                            category = category,
                            area = area,
                            owner = ownerName
                        )
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (fssaiNumber.isBlank()) {
                                    onShowMsg("FSSAI number is required")
                                    return@Button
                                }
                                onRegister(
                                    ownerName, ownerPhone, ownerPassword,
                                    restaurantName, category, address, area,
                                    openingTime, closingTime,
                                    minOrder.toIntOrNull() ?: 80,
                                    deliveryTime, fssaiNumber, gstNumber, selectedEmoji
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = indigo),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Submit Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    val indigo = Color(0xFF3949AB)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..totalSteps).forEach { i ->
            // Circle
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (i <= currentStep) indigo else Color(0xFFE0E0E0),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (i < currentStep) "✓" else "$i",
                        color = if (i <= currentStep) Color.White else Color(0xFF9E9E9E),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (i < currentStep) indigo else Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }
        }
    }
    // Step label
    val labels = listOf("Owner Details", "Restaurant Info", "Timings & Delivery", "Legal Documents")
    Text(
        "Step $currentStep: ${labels[currentStep - 1]}",
        modifier = Modifier.padding(horizontal = 24.dp),
        fontSize = 13.sp,
        color = Color(0xFF3949AB),
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
fun StepHeader(title: String, subtitle: String) {
    Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))
    Text(subtitle, fontSize = 14.sp, color = Color(0xFF757575), modifier = Modifier.padding(top = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("North Indian", "South Indian", "Local Special", "Snacks & Fast Food", "Sweets & Drinks", "Chinese", "Biryani", "Bakery", "Juice Bar")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.ifBlank { "Select Category" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Category *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3949AB),
                focusedLabelColor = Color(0xFF3949AB)
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = { onSelect(cat); expanded = false }
                )
            }
        }
    }
}

@Composable
fun EmojiPicker(selected: String, onSelect: (String) -> Unit) {
    val emojis = listOf("🍛", "🌾", "🍟", "🍮", "🥘", "🍜", "🍕", "🍰", "🥗", "🍱")
    Column {
        Text("Restaurant Icon", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF444))
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            emojis.forEach { emoji ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (emoji == selected) Color(0xFFE8EAF6) else Color(0xFFF5F5F5),
                    border = if (emoji == selected)
                        ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    else null,
                    onClick = { onSelect(emoji) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(emoji: String, name: String, category: String, area: String, owner: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFE8EAF6),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 40.sp)
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A237E))
                Text(category, fontSize = 13.sp, color = Color(0xFF3949AB))
                Text("📍 $area", fontSize = 12.sp, color = Color(0xFF757575))
                Text("👤 $owner", fontSize = 12.sp, color = Color(0xFF757575))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PartnerRegisterScreenPreview() {
    JashpurBhojanTheme {
        PartnerRegisterContent(
            step = 1,
            isLoading = false,
            snackbarMessage = null,
            onClearMessage = {},
            onSetStep = {},
            onShowMsg = {},
            onRegister = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StepIndicatorPreview() {
    JashpurBhojanTheme {
        Column(Modifier.padding(16.dp)) {
            StepIndicator(currentStep = 1, totalSteps = 4)
            Spacer(Modifier.height(20.dp))
            StepIndicator(currentStep = 2, totalSteps = 4)
            Spacer(Modifier.height(20.dp))
            StepIndicator(currentStep = 4, totalSteps = 4)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryCardPreview() {
    JashpurBhojanTheme {
        Box(Modifier.padding(16.dp)) {
            SummaryCard(
                emoji = "🍛",
                name = "Sharma Ji Ka Dhaba",
                category = "North Indian",
                area = "Jashpur Nagar",
                owner = "Ramesh Sharma"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmojiPickerPreview() {
    JashpurBhojanTheme {
        Box(Modifier.padding(16.dp)) {
            EmojiPicker(selected = "🍛", onSelect = {})
        }
    }
}
