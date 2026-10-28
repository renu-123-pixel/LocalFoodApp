package com.navigation

sealed class PartnerScreen(val route: String) {
    object Entry     : PartnerScreen("partner_entry")
    object Login     : PartnerScreen("partner_login")
    object Register  : PartnerScreen("partner_register")
    object Dashboard : PartnerScreen("partner_dashboard")
}
