package com.ui.screens.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.navigation.PartnerScreen
import com.ui.theme.JashpurBhojanTheme

// Entry screen — Choose Login or Register
@Composable
fun PartnerEntryScreen(navController: NavController) {
    PartnerEntryContent(
        onRegister = { navController.navigate(PartnerScreen.Register.route) },
        onLogin = { navController.navigate(PartnerScreen.Login.route) }
    )
}

@Composable
fun PartnerEntryContent(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A237E), Color(0xFF3949AB))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🏪", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))

            Text(
                "Jashpur Bhojan",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                "Partner Portal",
                fontSize = 18.sp,
                color = Color.White.copy(0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(0.15f)
            ) {
                Text(
                    "Reach thousands of customers\nin Jashpur Nagar 📍",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = Color.White.copy(0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(48.dp))

            // Register Button
            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    "Register Your Restaurant",
                    color = Color(0xFF1A237E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // Login Button
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text(
                    "Login to Dashboard",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // Benefits row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BenefitChip("🚀", "Fast\nOnboarding")
                BenefitChip("📦", "Order\nManagement")
                BenefitChip("📊", "Sales\nDashboard")
            }
        }
    }
}

@Composable
private fun BenefitChip(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(0.15f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 26.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
fun PartnerEntryScreenPreview() {
    JashpurBhojanTheme {
        PartnerEntryContent(onRegister = {}, onLogin = {})
    }
}
