package com.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ui.auth.AuthViewModel
import com.ui.auth.ForgotPasswordScreen
import com.ui.auth.OtpVerificationScreen
import com.ui.screens.*
import com.ui.screens.partner.*
import com.viewmodel.MainViewModel
import com.viewmodel.PartnerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    partnerViewModel: PartnerViewModel,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // ── Customer Screens ──────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                navController = navController
            )
        }
        composable(
            route = Screen.Otp.route,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStack ->
            val vId = backStack.arguments?.getString("verificationId") ?: ""
            val phone = backStack.arguments?.getString("phoneNumber") ?: ""
            OtpVerificationScreen(
                verificationId = vId,
                phoneNumber = phone,
                viewModel = authViewModel,
                navController = navController
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = Screen.Restaurant.route,
            arguments = listOf(navArgument("restaurantId") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("restaurantId") ?: return@composable
            val restaurants by viewModel.allAvailableRestaurants.collectAsState()
            val r = restaurants.find { it.id == id }
            if (r != null) {
                RestaurantScreen(restaurant = r, viewModel = viewModel, navController = navController)
            }
        }
        composable(Screen.Cart.route) {
            CartScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Tracking.route) {
            TrackingScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Orders.route) {
            OrdersScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = viewModel, navController = navController)
        }

        // ── Partner Screens ───────────────────────────────
        composable(PartnerScreen.Entry.route) {
            PartnerEntryScreen(navController = navController)
        }
        composable(PartnerScreen.Login.route) {
            PartnerLoginScreen(viewModel = partnerViewModel, navController = navController)
        }
        composable(PartnerScreen.Register.route) {
            PartnerRegisterScreen(viewModel = partnerViewModel, navController = navController)
        }
        composable(PartnerScreen.Dashboard.route) {
            PartnerDashboardScreen(viewModel = partnerViewModel, navController = navController)
        }
    }
}
