package com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.compose.rememberNavController
import com.navigation.Screen
import com.ui.components.AppTextField
import com.ui.components.PrimaryButton
import com.ui.theme.BackgroundCream
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.TextGray
import com.viewmodel.MainViewModel

@Composable
fun RegisterScreen(viewModel: MainViewModel, navController: NavController) {
    val message by viewModel.snackbarMessage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Navigate to home if registration is successful
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    RegisterContent(
        snackbarMessage = message,
        isLoading = isLoading,
        onClearMessage = { viewModel.clearMessage() },
        onRegister = { name, email, phone, password, address ->
            viewModel.register(name, email, phone, password, address)
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    snackbarMessage: String?,
    isLoading: Boolean,
    onClearMessage: () -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Account 🎉", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Join Jashpur Bhojan today", fontSize = 14.sp, color = TextGray)
            Spacer(Modifier.height(24.dp))

            AppTextField(name, { name = it }, "Full Name", "Your name", Icons.Default.Person)
            Spacer(Modifier.height(12.dp))
            AppTextField(email, { email = it }, "Email Address", "example@mail.com", Icons.Default.Email)
            Spacer(Modifier.height(12.dp))
            AppTextField(phone, { phone = it }, "Mobile Number", "10-digit number", Icons.Default.Phone)
            Spacer(Modifier.height(12.dp))
            AppTextField(password, { password = it }, "Password", "Create password", Icons.Default.Lock, isPassword = true)
            Spacer(Modifier.height(12.dp))
            AppTextField(address, { address = it }, "Delivery Address", "Your address in Jashpur", Icons.Default.Home)

            Spacer(Modifier.height(30.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PrimaryButton("Create Account") {
                    onRegister(name, email, phone, password, address)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    JashpurBhojanTheme {
        RegisterContent(
            snackbarMessage = null,
            isLoading = false,
            onClearMessage = {},
            onRegister = { _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
