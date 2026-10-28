package com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.data.model.Restaurant
import com.data.model.User
import com.navigation.PartnerScreen
import com.navigation.Screen
import com.ui.components.BottomNavBar
import com.ui.components.RestaurantCard
import com.ui.components.SectionHeader
import com.ui.theme.BackgroundCream
import com.ui.theme.CardWhite
import com.ui.theme.GreenPale
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.OrangeDeep
import com.ui.theme.OrangeLight
import com.ui.theme.TextDark
import com.ui.theme.TextGray
import com.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, navController: NavController) {
    val user by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val vegOnly by viewModel.vegOnly.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    val locationLabel by viewModel.locationLabel.collectAsState()

    // Consume all restaurants fetched from Firestore via MainViewModel
    val allRestaurants by viewModel.allAvailableRestaurants.collectAsState()
    
    // Apply local search and veg filtering
    val liveRestaurants = remember(allRestaurants, searchQuery, vegOnly) {
        allRestaurants.filter { r ->
            val q = searchQuery.lowercase()
            (q.isEmpty() || r.name.lowercase().contains(q) || r.category.lowercase().contains(q)) &&
            (!vegOnly || r.items.any { it.isVeg })
        }
    }

    HomeContent(
        user = user,
        searchQuery = searchQuery,
        vegOnly = vegOnly,
        cartCount = cartCount,
        locationLabel = locationLabel,
        liveRestaurants = liveRestaurants,
        onSearchQueryChange = viewModel::updateSearch,
        onToggleVegFilter = viewModel::toggleVegFilter,
        onNavigate = { route ->
            when (route) {
                "cart" -> navController.navigate(Screen.Cart.route)
                "orders" -> navController.navigate(Screen.Orders.route)
                "profile" -> navController.navigate(Screen.Profile.route)
                "partner" -> navController.navigate(PartnerScreen.Entry.route)
                else -> if (route.startsWith("restaurant/")) {
                    navController.navigate(route)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    user: User?,
    searchQuery: String,
    vegOnly: Boolean,
    cartCount: Int,
    locationLabel: String,
    liveRestaurants: List<Restaurant>,
    onSearchQueryChange: (String) -> Unit,
    onToggleVegFilter: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = "home", cartCount = cartCount) { route ->
                onNavigate(route)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding)
        ) {
            // ── Header ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(OrangeDeep, OrangeLight)),
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("📍 Delivering to", fontSize = 12.sp, color = Color.White.copy(0.8f))
                                Text(locationLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (cartCount > 0) {
                                    BadgedBox(
                                        badge = { Badge(containerColor = Color.White) { Text("$cartCount", color = OrangeDeep, fontWeight = FontWeight.Bold) } },
                                        modifier = Modifier.clickable { onNavigate("cart") }
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(0.2f),
                                    modifier = Modifier.clickable { onNavigate("profile") }
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp).size(22.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Hey ${user?.fullName?.split(" ")?.first() ?: "there"}! What are you craving? 😋", fontSize = 14.sp, color = Color.White.copy(0.9f))
                        Spacer(Modifier.height(12.dp))

                        // Search Bar
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (searchQuery.isEmpty()) Text("Search restaurants or food...", color = TextGray, fontSize = 14.sp)
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Partner CTA Banner ──
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onNavigate("partner") },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A237E)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Own a restaurant?", fontSize = 12.sp, color = Color.White.copy(0.7f))
                            Text("Partner with us 🚀", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Register & go live today", fontSize = 12.sp, color = Color.White.copy(0.8f))
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                            Text("Join →", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Promo Banner ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Brush.horizontalGradient(listOf(OrangeDeep, OrangeLight)), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Free Delivery 🎉", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("On orders above ₹150", fontSize = 13.sp, color = Color.White.copy(0.9f))
                        }
                        Text("🛵", fontSize = 44.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Categories ──
            item {
                SectionHeader("Categories", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("🍛" to "Thali", "🥘" to "Curry", "🌾" to "Local", "🍟" to "Snacks", "🍮" to "Sweets", "☕" to "Drinks").forEach { (e, l) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = RoundedCornerShape(16.dp), color = CardWhite, shadowElevation = 2.dp, modifier = Modifier.size(60.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text(e, fontSize = 26.sp) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(l, fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Restaurants Header ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SectionHeader("Restaurants Near You")
                        Text("${liveRestaurants.size} open now", fontSize = 12.sp, color = TextGray)
                    }
                    FilterChip(
                        selected = vegOnly, onClick = onToggleVegFilter,
                        label = { Text("🟢 Veg", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPale, selectedLabelColor = Color(0xFF2E7D32))
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Restaurant List ──
            if (liveRestaurants.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite
                    ) {
                        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🍽️", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No restaurants found", fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Try a different search", fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            } else {
                items(liveRestaurants) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        onNavigate("restaurant/${restaurant.id}")
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JashpurBhojanTheme {
        HomeContent(
            user = User(fullName = "Amit Kumar"),
            searchQuery = "",
            vegOnly = false,
            cartCount = 2,
            locationLabel = "Jashpur Nagar, CG",
            liveRestaurants = listOf(
                Restaurant(
                    id = 1,
                    name = "Sharma Ji Ka Dhaba",
                    category = "North Indian",
                    rating = 4.5,
                    deliveryTime = "25 min",
                    minOrder = 100,
                    emoji = "🍛",
                    address = "Main Market",
                    items = emptyList()
                ),
                Restaurant(
                    id = 2,
                    name = "Local Zayka",
                    category = "Chhattisgarhi",
                    rating = 4.8,
                    deliveryTime = "20 min",
                    minOrder = 80,
                    emoji = "🌾",
                    address = "Station Road",
                    items = emptyList()
                )
            ),
            onSearchQueryChange = {},
            onToggleVegFilter = {},
            onNavigate = {}
        )
    }
}
