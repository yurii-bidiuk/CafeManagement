package com.cafe.management.android.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Products : Screen("products")
    object Transaction : Screen("transaction")
    object TransactionSuccess : Screen("transaction_success")
    object TransactionHistory : Screen("transaction_history")
    object TransactionDetails : Screen("transaction_details")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
