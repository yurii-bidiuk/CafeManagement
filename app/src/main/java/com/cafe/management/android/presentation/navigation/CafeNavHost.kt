package com.cafe.management.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cafe.management.android.presentation.screens.history.TransactionHistoryScreen
import com.cafe.management.android.presentation.screens.login.LoginScreen
import com.cafe.management.android.presentation.screens.home.HomeScreen
import com.cafe.management.android.presentation.screens.products.CartItem
import com.cafe.management.android.presentation.screens.products.ProductsScreen
import com.cafe.management.android.presentation.screens.products.ProductsViewModel
import com.cafe.management.android.presentation.screens.splash.SplashScreen
import com.cafe.management.android.presentation.screens.transaction.TransactionScreen
import com.cafe.management.android.presentation.screens.transaction.TransactionSuccessScreen
import com.cafe.management.android.presentation.screens.transaction.details.TransactionDetailsScreen

@Composable
fun CafeNavHost(
    navController: NavHostController = rememberNavController()
) {
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProducts = {
                    navController.navigate(Screen.Products.route)
                },
                onNavigateToTransactionHistory = {
                    navController.navigate(Screen.TransactionHistory.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(route = Screen.Products.route) {
            val productsViewModel: ProductsViewModel = hiltViewModel()

            ProductsScreen(
                onNavigateToTransaction = {
                    cartItems = productsViewModel.getCartItems()
                    navController.navigate(Screen.Transaction.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = Screen.Transaction.route) {
            TransactionScreen(
                cartItems = cartItems,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionComplete = { transactionId ->
                    cartItems = emptyList()
                    navController.navigate("${Screen.TransactionSuccess.route}/$transactionId") {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = "${Screen.TransactionSuccess.route}/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionSuccessScreen(
                transactionId = transactionId,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNewTransaction = {
                    navController.navigate(Screen.Products.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(route = Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetails = { transactionId ->
                    navController.navigate("${Screen.TransactionDetails.route}/$transactionId")
                }
            )
        }

        composable(
            route = "${Screen.TransactionDetails.route}/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            TransactionDetailsScreen(
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
