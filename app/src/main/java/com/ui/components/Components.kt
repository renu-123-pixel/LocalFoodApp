package com.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.data.model.Restaurant
import com.ui.theme.CardWhite
import com.ui.theme.GreenVeg
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.OrangeDeep
import com.ui.theme.OrangePale
import com.ui.theme.RedNonVeg
import com.ui.theme.TextDark
import com.ui.theme.TextGray

// ─── Primary Button ───────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = OrangeDeep)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Outlined Input Field ─────────────────────────────────
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = TextGray) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = OrangeDeep) } },
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeDeep,
            focusedLabelColor = OrangeDeep,
            cursorColor = OrangeDeep
        ),
        singleLine = true
    )
}

// ─── Restaurant Card ──────────────────────────────────────
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(listOf(OrangePale, Color(0xFFFFE0D0)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(restaurant.emoji, fontSize = 52.sp)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = CardWhite
                ) {
                    Text(
                        "⭐ ${restaurant.rating}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8C00)
                    )
                }
            }
            // Info
            Column(modifier = Modifier.padding(14.dp)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(
                    "${restaurant.category} • ${restaurant.address}",
                    fontSize = 13.sp, color = TextGray,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip("🕐 ${restaurant.deliveryTime}")
                    InfoChip("🛵 ₹20 delivery")
                    InfoChip("📦 Min ₹${restaurant.minOrder}")
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Text(text, fontSize = 12.sp, color = TextGray)
}

// ─── Section Header ───────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = modifier
    )
}

// ─── Veg / Non-Veg Indicator ─────────────────────────────
@Composable
fun VegIndicator(isVeg: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.5.dp, if (isVeg) GreenVeg else RedNonVeg, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isVeg) GreenVeg else RedNonVeg)
        )
    }
}

// ─── Quantity Stepper ─────────────────────────────────────
@Composable
fun QuantityStepper(
    qty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    if (qty == 0) {
        Surface(
            modifier = Modifier
                .clickable { onAdd() }
                .clip(RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = OrangeDeep
        ) {
            Text(
                "ADD",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    } else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeDeep)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Text(
                "$qty",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.widthIn(min = 20.dp),
            )
            IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    currentRoute: String,
    cartCount: Int,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = CardWhite, tonalElevation = 8.dp) {
        listOf(
            Triple("home", Icons.Default.Home, "Home"),
            Triple("cart", Icons.Default.ShoppingCart, "Cart"),
            Triple("orders", Icons.Default.List, "Orders"),
            Triple("profile", Icons.Default.Person, "Profile")
        ).forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = {
                    BadgedBox(badge = {
                        if (route == "cart" && cartCount > 0) {
                            Badge(containerColor = OrangeDeep) { Text("$cartCount") }
                        }
                    }) {
                        Icon(icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeDeep,
                    selectedTextColor = OrangeDeep,
                    indicatorColor = OrangePale
                )
            )
        }
    }
}

// ─── Bill Row ─────────────────────────────────────────────
@Composable
fun BillRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = if (isBold) TextDark else TextGray)
        Text(value, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium, color = if (isBold) OrangeDeep else TextDark)
    }
}

// ─── Previews ─────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun RestaurantCardPreview() {
    JashpurBhojanTheme {
        RestaurantCard(
            restaurant = Restaurant(
                id = 1,
                name = "Jashpur Delights",
                category = "North Indian",
                address = "Main Road, Jashpur",
                rating = 4.5,
                deliveryTime = "30 mins",
                minOrder = 100,
                items = emptyList(),
                emoji = "🍲"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
    JashpurBhojanTheme {
        PrimaryButton(text = "Order Now", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun QuantityStepperPreview() {
    JashpurBhojanTheme {
        QuantityStepper(qty = 2, onAdd = {}, onRemove = {})
    }
}
