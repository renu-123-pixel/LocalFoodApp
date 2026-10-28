package com.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.navigation.Screen
import com.ui.components.PrimaryButton
import com.ui.theme.BackgroundCream
import com.ui.theme.OrangeDeep
import com.ui.theme.TextDark
import com.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    verificationId: String,
    phoneNumber: String,
    viewModel: AuthViewModel,
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }
    val signInState by viewModel.signInState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(signInState) {
        when (signInState) {
            is AuthState.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((signInState as AuthState.Error).message)
                viewModel.clearStates()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "We've sent a 6-digit code to",
                fontSize = 14.sp,
                color = TextGray
            )
            Text(
                phoneNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("Enter OTP") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeDeep,
                    cursorColor = OrangeDeep
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "Verify & Login",
                onClick = {
                    if (otpCode.length == 6) {
                        viewModel.verifyOtp(verificationId, otpCode)
                    }
                }
            )

            if (signInState is AuthState.Loading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = OrangeDeep)
            }

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = { /* Handle Resend OTP */ }) {
                Text("Didn't receive code? Resend", color = OrangeDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}
