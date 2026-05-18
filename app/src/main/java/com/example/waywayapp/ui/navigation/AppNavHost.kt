package com.example.waywayapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.waywayapp.ui.auth.login.LoginScreen
import com.example.waywayapp.ui.auth.register.RegisterScreen
import com.example.waywayapp.ui.driver.home.DriverHomeScreen
import com.example.waywayapp.ui.user.booking.BookingRoute
import com.example.waywayapp.ui.user.booking.food.FoodBookingScreen
import com.example.waywayapp.ui.user.booking.food.cart.FoodCartScreen
import com.example.waywayapp.ui.user.home.HomeScreen
import com.example.waywayapp.ui.user.notification.NotificationScreen
import com.example.waywayapp.ui.user.payment.AddPaymentScreen
import com.example.waywayapp.ui.user.profile.ProfileScreen

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
        //Login
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            // Your Home Screen
            HomeScreen(
                currentRoute = currentRoute,
                onServiceClick = {
                    type -> navController.navigate(Routes.createBookingRoute(type))
                },
                onWalletClick = {
                    navController.navigate("add_payment")
                },
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onFoodClick = { foodId ->

                    navController.navigate(
                        Routes.createFoodRoute(foodId)
                    )
                }
            )
        }
        composable(Routes.BOOKING,
            arguments = listOf(
                navArgument("type"){
                    defaultValue = "bike"
                    type = NavType.StringType
                },

            )

        ) { backStackEntry ->
            val bookingType = backStackEntry.arguments?.getString("type") ?: "bike"
            BookingRoute(type = bookingType,
                onBackClick = {
                    navController.popBackStack()
                },
                onCartClick = {
                    navController.navigate(Routes.FOOD_CART)
                }
            )


        }

        composable(Routes.NOTIFICATION) {
            NotificationScreen()
        }
        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(
            route = Routes.FOOD,
            arguments = listOf(
                navArgument("foodId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val foodId =
                backStackEntry.arguments?.getInt("foodId") ?: 0

            FoodBookingScreen(
                selectedFoodId = foodId,
                onBackClick = {
                    navController.popBackStack()
                },
                onCartClick = {
                    navController.navigate(Routes.FOOD_CART)
                }
            )
        }
        composable(Routes.FOOD_CART) {
            FoodCartScreen(
                onBackClick = {
                    navController.popBackStack()
                }

            )
        }
        composable("add_payment") {
            AddPaymentScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMomoClick = {
                    navController.navigate("momo_payment")
                },
                onBankClick = {
                    navController.navigate("bank_payment")
                }
            )
        }
    }
}
