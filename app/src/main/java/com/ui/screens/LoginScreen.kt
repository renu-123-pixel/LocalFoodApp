package com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.navigation.Screen
import com.ui.components.AppTextField
import com.ui.components.PrimaryButton
import com.ui.theme.BackgroundCream
import com.ui.theme.JashpurBhojanTheme
import com.ui.theme.OrangeDeep
import com.ui.theme.TextDark
import com.ui.theme.TextGray
import com.viewmodel.MainViewModel

@Composable
fun LoginScreen(viewModel: MainViewModel, navController: NavController) {
    val message by viewModel.snackbarMessage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Navigate to home if login is successful
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    LoginContent(
        snackbarMessage = message,
        isLoading = isLoading,
        onClearMessage = { viewModel.clearMessage() },
        onLogin = { email, password ->
            viewModel.login(email, password)
        },
        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
    )
}

@Composable
fun LoginContent(
    snackbarMessage: String?,
    isLoading: Boolean,
    onClearMessage: () -> Unit,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Text("🍱", fontSize = 64.sp)
            Text(
                "Jashpur Bhojan",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = OrangeDeep
            )
            Text("Your local food delivery app", fontSize = 14.sp, color = TextGray)

            Spacer(Modifier.height(40.dp))

            Text(
                "Welcome Back! 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email
            )
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )
            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                PrimaryButton(text = "Login", onClick = {
                    onLogin(email, password)
                })
            }

            Spacer(Modifier.height(16.dp))

            Text(
                buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(SpanStyle(color = OrangeDeep, fontWeight = FontWeight.Bold)) {
                        append("Register here")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    JashpurBhojanTheme {
        LoginContent(
            snackbarMessage = null,
            isLoading = false,
            onClearMessage = {},
            onLogin = { _, _ -> },
            onNavigateToRegister = {}
        )
    }
}
