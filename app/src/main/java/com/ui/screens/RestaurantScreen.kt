package com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.data.model.FoodItem
import com.data.model.Restaurant
import com.navigation.Screen
import com.ui.components.QuantityStepper
import com.ui.components.SectionHeader
import com.ui.components.VegIndicator
import com.ui.theme.BackgroundCream
import com.ui.theme.CardWhite
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.OrangeDeep
import com.ui.theme.OrangePale
import com.ui.theme.TextDark
import com.ui.theme.TextGray
import com.viewmodel.MainViewModel

@Composable
fun RestaurantScreen(
    restaurant: Restaurant,
    viewModel: MainViewModel,
    navController: NavController
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    val cartTotal by remember { derivedStateOf { viewModel.cartTotal } }
    val message by viewModel.snackbarMessage.collectAsState()

    RestaurantContent(
        restaurant = restaurant,
        cartCount = cartCount,
        cartTotal = cartTotal,
        snackbarMessage = message,
        getItemQty = { viewModel.getItemQty(it) },
        onClearMessage = { viewModel.clearMessage() },
        onAddToCart = { item -> viewModel.addToCart(item, restaurant) },
        onRemoveFromCart = { viewModel.removeFromCart(it) },
        onClearCartAndAdd = { item -> viewModel.clearCartAndAdd(item, restaurant) },
        onBack = { navController.popBackStack() },
        onNavigateToCart = { navController.navigate(Screen.Cart.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantContent(
    restaurant: Restaurant,
    cartCount: Int,
    cartTotal: Int,
    snackbarMessage: String?,
    getItemQty: (Int) -> Int,
    onClearMessage: () -> Unit,
    onAddToCart: (FoodItem) -> Boolean,
    onRemoveFromCart: (Int) -> Unit,
    onClearCartAndAdd: (FoodItem) -> Unit,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearCartDialog by remember { mutableStateOf<FoodItem?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    // Clear cart dialog
    showClearCartDialog?.let { pendingItem ->
        AlertDialog(
            onDismissRequest = { showClearCartDialog = null },
            title = { Text("Clear Cart?") },
            text = { Text("Your cart has items from another restaurant. Clear it and add this item?") },
            confirmButton = {
                TextButton(onClick = {
                    onClearCartAndAdd(pendingItem)
                    showClearCartDialog = null
                }) { Text("Clear & Add", color = OrangeDeep) }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartDialog = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(BackgroundCream)) {
                // Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFE0CC), Color(0xFFFFF3E0)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(restaurant.emoji, fontSize = 80.sp)
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .background(Color.White, RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }

                // Restaurant Info
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CardWhite
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(restaurant.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                            Text("${restaurant.category} • ${restaurant.address}", fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                Text("⭐ ${restaurant.rating}", fontWeight = FontWeight.Bold, color = Color(0xFFFF8C00), fontSize = 14.sp)
                                Text("🕐 ${restaurant.deliveryTime}", fontSize = 13.sp, color = TextGray)
                                Text("📦 Min ₹${restaurant.minOrder}", fontSize = 13.sp, color = TextGray)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Menu Header
                item {
                    SectionHeader(
                        "Menu",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Menu Items
                items(restaurant.items) { item ->
                    FoodItemCard(
                        item = item,
                        qty = getItemQty(item.id),
                        onAdd = {
                            val added = onAddToCart(item)
                            if (!added) showClearCartDialog = item
                        },
                        onRemove = { onRemoveFromCart(item.id) }
                    )
                }

                item { Spacer(Modifier.height(90.dp)) }
            }

            // Sticky Cart Button
            if (cartCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = OrangeDeep,
                    shadowElevation = 8.dp,
                    onClick = onNavigateToCart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$cartCount item${if (cartCount > 1) "s" else ""} · ₹$cartTotal", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("View Cart →", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(item: FoodItem, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VegIndicator(item.isVeg)
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    if (item.isPopular) {
                        Surface(shape = RoundedCornerShape(8.dp), color = OrangePale) {
                            Text("Popular", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(item.description, fontSize = 13.sp, color = TextGray, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Text("₹${item.price}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = OrangeDeep)
            }
            Spacer(Modifier.width(12.dp))
            QuantityStepper(qty = qty, onAdd = onAdd, onRemove = onRemove)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantScreenPreview() {
    JashpurBhojanTheme {
        RestaurantContent(
            restaurant = Restaurant(
                id = 1,
                name = "Jashpur Delights",
                category = "North Indian",
                rating = 4.5,
                deliveryTime = "30m",
                minOrder = 100,
                emoji = "🍲",
                address = "Main Road",
                items = listOf(
                    FoodItem(1, "Paneer Tikka", 150, "Grilled cottage cheese", true, true, 1),
                    FoodItem(2, "Chicken Curry", 200, "Spicy chicken gravy", false, true, 1)
                )
            ),
            cartCount = 2,
            cartTotal = 350,
            snackbarMessage = null,
            getItemQty = { 1 },
            onClearMessage = {},
            onAddToCart = { true },
            onRemoveFromCart = {},
            onClearCartAndAdd = {},
            onBack = {},
            onNavigateToCart = {}
        )
    }
}
