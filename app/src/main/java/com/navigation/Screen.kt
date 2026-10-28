package com.navigation

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Login         : Screen("login")
    object Register      : Screen("register")
    object Otp           : Screen("otp/{verificationId}/{phoneNumber}") {
        fun createRoute(verificationId: String, phoneNumber: String) = "otp/$verificationId/$phoneNumber"
    }
    object ForgotPassword : Screen("forgot_password")
    object Home          : Screen("home")
    object Restaurant    : Screen("restaurant/{restaurantId}") {
        fun createRoute(id: Int) = "restaurant/$id"
    }
    object Cart          : Screen("cart")
    object Checkout      : Screen("checkout")
    object Tracking      : Screen("tracking")
    object Orders        : Screen("orders")
    object Profile       : Screen("profile")
}
