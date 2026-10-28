package com.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.data.model.*
import com.data.repository.AppRepository
import com.navigation.Screen
import com.ui.components.BillRow
import com.ui.components.BottomNavBar
import com.ui.components.PrimaryButton
import com.ui.components.QuantityStepper
import com.ui.theme.BackgroundCream
import com.ui.theme.CardWhite
import com.ui.theme.DividerGray
import com.ui.theme.GreenPale
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.OrangeDeep
import com.ui.theme.OrangeLight
import com.ui.theme.OrangePale
import com.ui.theme.TextDark
import com.ui.theme.TextGray
import com.viewmodel.MainViewModel

// ═══════════════════════════════════════════════════════════
// CART SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun CartScreen(viewModel: MainViewModel, navController: NavController) {
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    val user by viewModel.currentUser.collectAsState()

    CartContent(
        cartItems = cartItems,
        cartCount = cartCount,
        cartTotal = viewModel.cartTotal,
        deliveryFee = viewModel.deliveryFee,
        platformFee = viewModel.platformFee,
        grandTotal = viewModel.grandTotal,
        userAddress = user?.address ?: "Jashpur Nagar, CG 496331",
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) },
        onAddToCart = { item, restaurantId ->
            val restaurant = AppRepository.restaurants.find { it.id == restaurantId }!!
            viewModel.addToCart(item, restaurant)
        },
        onRemoveFromCart = { viewModel.removeFromCart(it) },
        onProceedToPay = { navController.navigate(Screen.Checkout.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    cartItems: List<CartItem>,
    cartCount: Int,
    cartTotal: Int,
    deliveryFee: Int,
    platformFee: Int,
    grandTotal: Int,
    userAddress: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onAddToCart: (FoodItem, Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onProceedToPay: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart 🛒", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "cart", cartCount = cartCount) { route ->
                onNavigate(route)
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 72.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Add items to get started", color = TextGray, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = "Browse Restaurants",
                        onClick = { onNavigate(Screen.Home.route) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restaurant label
                item {
                    Surface(shape = RoundedCornerShape(10.dp), color = OrangePale, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFFE65100))
                            Text(cartItems.first().restaurantName, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                        }
                    }
                }

                // Cart items
                items(cartItems) { cartItem ->
                    Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cartItem.foodItem.name, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("₹${cartItem.foodItem.price} × ${cartItem.quantity} = ₹${cartItem.foodItem.price * cartItem.quantity}", color = OrangeDeep, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                            QuantityStepper(
                                qty = cartItem.quantity,
                                onAdd = { onAddToCart(cartItem.foodItem, cartItem.restaurantId) },
                                onRemove = { onRemoveFromCart(cartItem.foodItem.id) }
                            )
                        }
                    }
                }

                // Bill Summary
                item {
                    Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                            BillRow("Item Total", "₹$cartTotal")
                            BillRow("Delivery Fee", "₹$deliveryFee")
                            BillRow("Platform Fee", "₹$platformFee")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerGray)
                            BillRow("Grand Total", "₹$grandTotal", isBold = true)
                        }
                    }
                }

                // Delivery Address
                item {
                    Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangeDeep)
                                Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(userAddress, color = TextGray, fontSize = 14.sp)
                        }
                    }
                }

                // Proceed Button
                item {
                    PrimaryButton(
                        text = "Proceed to Pay ₹$grandTotal",
                        onClick = onProceedToPay
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CHECKOUT SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun CheckoutScreen(viewModel: MainViewModel, navController: NavController) {
    CheckoutContent(
        grandTotal = viewModel.grandTotal,
        onBack = { navController.popBackStack() },
        onPlaceOrder = {
            viewModel.placeOrder()
            navController.navigate(Screen.Tracking.route) {
                popUpTo(Screen.Home.route)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutContent(
    grandTotal: Int,
    onBack: () -> Unit,
    onPlaceOrder: () -> Unit
) {
    var selectedPayment by remember { mutableIntStateOf(0) }
    val paymentMethods = listOf(
        Triple("💳", "UPI / GPay / PhonePe", "Instant & Secure"),
        Triple("💵", "Cash on Delivery", "Pay when delivered"),
        Triple("🏦", "Net Banking", "All banks supported")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment 💳", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Choose Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                        paymentMethods.forEachIndexed { index, (emoji, method, sub) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method, fontWeight = FontWeight.SemiBold, color = TextDark)
                                    Text(sub, fontSize = 12.sp, color = TextGray)
                                }
                                RadioButton(
                                    selected = selectedPayment == index,
                                    onClick = { selectedPayment = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = OrangeDeep)
                                )
                            }
                            if (index < paymentMethods.lastIndex) HorizontalDivider(color = DividerGray)
                        }
                    }
                }
            }

            item {
                Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BillRow("Total Payable", "₹$grandTotal", isBold = true)
                    }
                }
            }

            item {
                PrimaryButton("Pay ₹$grandTotal & Place Order", onClick = onPlaceOrder)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("100% Secure Payment", fontSize = 12.sp, color = TextGray)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// TRACKING SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun TrackingScreen(viewModel: MainViewModel, navController: NavController) {
    val order by viewModel.currentOrder.collectAsState()
    val trackingStep by viewModel.trackingStep.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    TrackingContent(
        order = order,
        trackingStep = trackingStep,
        cartCount = cartCount,
        onNavigate = { route -> navController.navigate(route) },
        onRateOrder = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    )
}

@Composable
fun TrackingContent(
    order: Order?,
    trackingStep: Int,
    cartCount: Int,
    onNavigate: (String) -> Unit,
    onRateOrder: () -> Unit
) {
    val statuses = OrderStatus.values()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "tracking",
                cartCount = cartCount) { route ->
                onNavigate(route)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(OrangeDeep, OrangeLight)), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text("Live Tracking 📍", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Order #${order?.id ?: ""}", fontSize = 14.sp, color = Color.White.copy(0.85f))
                    }
                }
            }

            // Map Placeholder
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", fontSize = 40.sp)
                            Text("Jashpur Nagar, CG", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                            Text("📍 Live location tracking active", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🏪", fontSize = 20.sp)
                                Text("- - - 🛵 - - -", color = OrangeDeep, fontWeight = FontWeight.Bold)
                                Text("🏠", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // Order Status Steps
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order Status", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))
                        statuses.forEachIndexed { index, status ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (index <= trackingStep) OrangeDeep else DividerGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (index <= trackingStep) "✓" else "${index + 1}",
                                        color = if (index <= trackingStep) Color.White else TextGray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${status.emoji} ${status.label}",
                                    fontSize = 14.sp,
                                    fontWeight = if (index == trackingStep) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index <= trackingStep) TextDark else TextGray,
                                    modifier = Modifier.weight(1f)
                                )
                                if (index == trackingStep) {
                                    Surface(shape = RoundedCornerShape(20.dp), color = OrangePale) {
                                        Text("● Live", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = OrangeDeep, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delivery Partner
            order?.deliveryPartner?.let { partner ->
                item {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        shadowElevation = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Your Delivery Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = OrangePale, modifier = Modifier.size(52.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("🧑", fontSize = 26.sp) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(partner.name, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text("⭐ ${partner.rating} • 🏍️ ${partner.vehicleNumber}", fontSize = 13.sp, color = TextGray)
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = GreenPale,
                                    onClick = {}
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF2E7D32), modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Rate button when delivered
            if (trackingStep == statuses.lastIndex) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PrimaryButton("Rate Your Order ⭐", onClick = onRateOrder)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ORDERS SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun OrdersScreen(viewModel: MainViewModel, navController: NavController) {
    val orders by viewModel.orders.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    OrdersContent(
        orders = orders,
        cartCount = cartCount,
        onNavigate = { route -> navController.navigate(route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersContent(
    orders: List<Order>,
    cartCount: Int,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders 📦", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundCream)
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "orders",
                cartCount = cartCount) { route ->
                onNavigate(route)
            }
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 72.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No orders yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton("Order Now", modifier = Modifier.width(160.dp)) {
                        onNavigate(Screen.Home.route)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders) { order ->
                    Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${order.id}", fontWeight = FontWeight.Bold, color = TextDark)
                                Text("${order.date} ${order.time}", fontSize = 12.sp, color = TextGray)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("🏪 ${order.restaurantName}", fontSize = 14.sp, color = TextGray)
                            Text(order.items.joinToString(", ") { it.foodItem.name }, fontSize = 13.sp, color = TextGray, maxLines = 2, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("₹${order.totalAmount}", fontWeight = FontWeight.ExtraBold, color = OrangeDeep, fontSize = 16.sp)
                                Surface(shape = RoundedCornerShape(20.dp), color = GreenPale) {
                                    Text("Delivered ✓", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PROFILE SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
fun ProfileScreen(viewModel: MainViewModel, navController: NavController) {
    val user by viewModel.currentUser.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    ProfileContent(
        user = user,
        orderCount = orders.size,
        cartCount = cartCount,
        onNavigate = { route -> navController.navigate(route) },
        onLogout = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}

@Composable
fun ProfileContent(
    user: User?,
    orderCount: Int,
    cartCount: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "profile",
                cartCount = cartCount) { route ->
                onNavigate(route)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundCream).padding(padding)
        ) {
            // Profile Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(OrangeDeep, OrangeLight)), RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                        .padding(horizontal = 16.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color.White.copy(0.2f), modifier = Modifier.size(80.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 40.sp) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(user?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("📱 ${user?.phone ?: ""}", fontSize = 14.sp, color = Color.White.copy(0.85f))
                    }
                }
            }

            // Info Cards
            item {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(Icons.Default.LocationOn, "Delivery Address", user?.address ?: "Jashpur Nagar, CG 496331"),
                        Triple(Icons.Default.ShoppingBag, "Total Orders", "$orderCount orders placed"),
                        Triple(Icons.Default.Star, "Membership", "Jashpur Bhojan Member"),
                        Triple(Icons.Default.Place, "Delivery Zone", "Jashpur Nagar, CG 496331")
                    ).forEach { (icon, label, value) ->
                        Surface(shape = RoundedCornerShape(14.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(shape = CircleShape, color = OrangePale, modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = OrangeDeep, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Column {
                                    Text(label, fontSize = 12.sp, color = TextGray)
                                    Text(value, fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Logout
            item {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    JashpurBhojanTheme {
        CartContent(
            cartItems = listOf(
                CartItem(
                    FoodItem(1, "Paneer Tikka", 150, "Cottage cheese", true, true, 1),
                    1,
                    "Jashpur Delights",
                    2
                )
            ),
            cartCount = 2,
            cartTotal = 300,
            deliveryFee = 20,
            platformFee = 5,
            grandTotal = 325,
            userAddress = "Main Market, Jashpur Nagar",
            onBack = {},
            onNavigate = {},
            onAddToCart = { _, _ -> },
            onRemoveFromCart = {},
            onProceedToPay = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    JashpurBhojanTheme {
        CheckoutContent(grandTotal = 325, onBack = {}, onPlaceOrder = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TrackingScreenPreview() {
    JashpurBhojanTheme {
        TrackingContent(
            order = Order(
                "JSP123456", emptyList(), "Jashpur Delights", 325, "Address",
                DeliveryPartner(1, "Ramesh", "9876543210", 4.5, "CG10-1234"),
                "10:00 AM", "01 Jan 2024"
            ),
            trackingStep = 2,
            cartCount = 2,
            onNavigate = {},
            onRateOrder = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrdersScreenPreview() {
    JashpurBhojanTheme {
        OrdersContent(
            orders = listOf(
                Order(
                    "JSP123456", emptyList(), "Jashpur Delights", 325, "Address",
                    DeliveryPartner(1, "Ramesh", "9876543210", 4.5, "CG10-1234"),
                    "10:00 AM", "01 Jan 2024"
                )
            ),
            cartCount = 0,
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    JashpurBhojanTheme {
        ProfileContent(
            user = User("Jashpur User", "1234567890", "pass", "Main Market, Jashpur"),
            orderCount = 5,
            cartCount = 0,
            onNavigate = {},
            onLogout = {}
        )
    }
}
