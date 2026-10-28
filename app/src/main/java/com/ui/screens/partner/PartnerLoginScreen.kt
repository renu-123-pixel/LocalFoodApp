package com.ui.screens.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.navigation.PartnerScreen
import com.ui.components.AppTextField
import com.viewmodel.PartnerViewModel
import com.ui.theme.JashpurBhojanTheme

@Composable
fun PartnerLoginScreen(viewModel: PartnerViewModel, navController: NavController) {
    val message by viewModel.message.collectAsState()
    val loggedIn by viewModel.loggedInRestaurant.collectAsState()

    LaunchedEffect(loggedIn) {
        if (loggedIn != null) {
            navController.navigate(PartnerScreen.Dashboard.route) {
                popUpTo(PartnerScreen.Entry.route)
            }
        }
    }

    PartnerLoginContent(
        snackbarMessage = message,
        onClearMessage = { viewModel.clearMessage() },
        onLogin = { phone, password -> viewModel.login(phone, password) },
        onNavigateToRegister = { navController.navigate(PartnerScreen.Register.route) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerLoginContent(
    snackbarMessage: String?,
    onClearMessage: () -> Unit,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val indigo = Color(0xFF3949AB)

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbar.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB))))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text("🏪", fontSize = 64.sp)
            Text("Partner Login", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Manage your restaurant", fontSize = 14.sp, color = Color.White.copy(0.8f))
            Spacer(Modifier.height(36.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AppTextField(phone, { phone = it }, "Registered Mobile", "Enter phone number", Icons.Default.Phone)
                    Spacer(Modifier.height(12.dp))
                    AppTextField(password, { password = it }, "Password", "Enter password", Icons.Default.Lock, isPassword = true)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onLogin(phone, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = indigo)
                    ) { Text("Login to Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        buildAnnotatedString {
                            append("New partner? ")
                            withStyle(SpanStyle(color = indigo, fontWeight = FontWeight.Bold)) {
                                append("Register your restaurant")
                            }
                        },
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .align(Alignment.CenterHorizontally),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Demo credentials hint
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(0.15f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 Demo Credentials", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    listOf(
                        "📱 9876500001 • sharma123 — Sharma Ji Ka Dhaba",
                        "📱 9876500002 • zayka123 — Chhattisgarhi Zayka"
                    ).forEach {
                        Text(it, color = Color.White.copy(0.85f), fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PartnerLoginScreenPreview() {
    JashpurBhojanTheme {
        PartnerLoginContent(
            snackbarMessage = null,
            onClearMessage = {},
            onLogin = { _, _ -> },
            onNavigateToRegister = {}
        )
    }
}
