package com.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ui.components.AppTextField
import com.ui.components.PrimaryButton
import com.ui.theme.BackgroundCream
import com.ui.theme.OrangeDeep
import com.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    navController: NavController
) {
    val resetState by viewModel.resetPasswordState.collectAsState()
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resetState) {
        when (resetState) {
            is AuthState.Success -> {
                snackbarHostState.showSnackbar("Reset email sent! Check your inbox.")
                viewModel.clearStates()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((resetState as AuthState.Error).message)
                viewModel.clearStates()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
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
                "Enter your registered email and we'll send you a link to reset your password.",
                fontSize = 14.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "example@gmail.com",
                leadingIcon = Icons.Default.Email
            )

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "Send Reset Link",
                onClick = {
                    if (email.isNotBlank()) {
                        viewModel.resetPassword(email.trim())
                    }
                }
            )

            if (resetState is AuthState.Loading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = OrangeDeep)
            }
        }
    }
}
