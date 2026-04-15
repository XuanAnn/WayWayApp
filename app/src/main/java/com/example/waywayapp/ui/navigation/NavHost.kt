package com.example.waywayapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.waywayapp.ui.auth.login.LoginScreen
import com.example.waywayapp.ui.auth.register.RegisterScreen
import com.example.waywayapp.ui.user.booking.BookingScreen
import com.example.waywayapp.ui.user.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.USER_HOME,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Placeholder for Home
        composable(Routes.USER_HOME) {
            // Your Home Screen
            HomeScreen(
                onServiceClick = {
                    serviceName ->
                    when (serviceName) {
                        "Xe máy" -> navController.navigate(Routes.createBookingRoute("bike"))
                        "Đồ ăn" -> navController.navigate(Routes.createBookingRoute("food"))
                        "Giao hàng" -> navController.navigate(Routes.createBookingRoute("express"))
                    }
                     },
            )
        }
        composable(Routes.BOOKING,
            arguments = listOf(
                navArgument("type"){
                    defaultValue = "bike"
                    type = NavType.StringType
                }

            )
        ) { backStackEntry ->
            val bookingType = backStackEntry.arguments?.getString("type") ?: "bike"
            BookingScreen(type = bookingType)
        }

    }
}
