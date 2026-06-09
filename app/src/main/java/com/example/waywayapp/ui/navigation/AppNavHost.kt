package com.example.waywayapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.ui.admin.driver.AdminDriverScreen
import com.example.waywayapp.ui.ai.AiAssistantScreen
import com.example.waywayapp.ui.chat.RideChatScreen
import com.example.waywayapp.ui.driver.home.DriverHomeScreen
import com.example.waywayapp.ui.driver.income.DriverIncomeScreen
import com.example.waywayapp.ui.auth.login.LoginScreen
import com.example.waywayapp.ui.auth.register.RegisterScreen
import com.example.waywayapp.ui.user.booking.BookingRoute
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.example.waywayapp.ui.user.booking.express.picker.LocationPickerScreen
import com.example.waywayapp.ui.user.home.HomeScreen
import com.example.waywayapp.ui.user.notification.NotificationScreen
import com.example.waywayapp.ui.user.payment.AddPaymentScreen
import com.example.waywayapp.ui.user.payment.MomoLinkScreen
import com.example.waywayapp.ui.user.profile.ProfileScreen
import com.example.waywayapp.ui.user.rating.RideRatingScreen
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.user.booking.bike.search.BikeSearchScreen
import com.example.waywayapp.ui.user.booking.bike.map.BikeLocationPickerScreen
import com.example.waywayapp.ui.user.booking.bike.confirm.BikeConfirmScreen
import com.example.waywayapp.ui.user.booking.bike.model.BikeLocationType
import com.example.waywayapp.ui.user.history.HistoryScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authRepository = FirebaseAuthRepository()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination by produceState<String?>(
        initialValue = null,
        key1 = currentUser
    ) {
        value = if (currentUser == null) {
            Routes.LOGIN
        } else {
            runCatching {
                routeForRole(authRepository.getCurrentUserRole())
            }.getOrElse {
                authRepository.signOut()
                Routes.LOGIN
            }
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination.orEmpty(),
        modifier = modifier
    ) {
        /*----------------------------------------------------
        AUTH
        ------------------------------------------------------*/
        //LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(routeForRole(role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        //REGISTER
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

        composable(Routes.ADMIN_DRIVERS) {
            AdminDriverScreen(
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeScreen(
                onIncomeClick = {
                    navController.navigate(Routes.DRIVER_INCOME) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onOpenChat = { rideId ->
                    navController.navigate(Routes.createRideChatRoute(rideId))
                },
                onAiAssistantClick = {
                    navController.navigate(Routes.createAiAssistantRoute("DRIVER"))
                }
            )
        }

        composable(Routes.DRIVER_INCOME) {
            DriverIncomeScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Routes.DRIVER_HOME) {
                        popUpTo(Routes.DRIVER_HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
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
                onAiAssistantClick = {
                    navController.navigate(Routes.createAiAssistantRoute("USER"))
                },
                onSearchClick = {
                    navController.navigate(Routes.BIKE_SEARCH) {
                        launchSingleTop = true
                    }
                },
                onSearchSuggestionClick = { address ->
                    BikeSharedViewModel.viewModel.selectServiceType("bike")
                    BikeSharedViewModel.viewModel.onSearchQueryChange(address)
                    BikeSharedViewModel.viewModel.searchLocation(address)
                    navController.navigate(Routes.BIKE_CONFIRM) {
                        launchSingleTop = true
                    }
                },
                onNotificationClick = {
                    navController.navigate(Routes.NOTIFICATION) {
                        launchSingleTop = true
                    }
                },
                onPromoClick = {
                    navController.navigate("momo_payment") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Routes.AI_ASSISTANT,
            arguments = listOf(
                navArgument("role") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            AiAssistantScreen(
                role = backStackEntry.arguments?.getString("role").orEmpty(),
                onBackClick = {
                    navController.popBackStack()
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
            BookingRoute(
                type = bookingType,
                onBackClick = {
                    navController.popBackStack()
                },
                onBikePickupClick = {
                    navController.navigate(Routes.BIKE_PICKUP_MAP)
                },
                onBikeDropoffClick = {
                    navController.navigate(Routes.BIKE_DROPOFF_MAP)
                },
                onBikeConfirmClick = {
                    navController.navigate(Routes.BIKE_CONFIRM)
                },
                onExpressPickupClick = {
                    navController.navigate(Routes.EXPRESS_PICKUP)
                },
                onExpressDropoffClick = {
                    navController.navigate(Routes.EXPRESS_DROPOFF)
                },
                onExpressConfirmClick = {
                    navController.navigate(Routes.EXPRESS_CONFIRM)
                },
            )
        }
        composable(Routes.BIKE_SEARCH) {
            BikeSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPickupMapClick = {
                    navController.navigate(Routes.BIKE_PICKUP_MAP)
                },
                onDropoffMapClick = {
                    navController.navigate(Routes.BIKE_DROPOFF_MAP)
                },
                onConfirmClick = {
                    navController.navigate(Routes.BIKE_CONFIRM)
                }
            )
        }

        composable(Routes.BIKE_PICKUP_MAP) {
            BikeLocationPickerScreen(
                type = BikeLocationType.PICKUP,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.BIKE_DROPOFF_MAP) {
            BikeLocationPickerScreen(
                type = BikeLocationType.DROPOFF,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.navigate(Routes.BIKE_CONFIRM)
                }
            )
        }

        composable(Routes.BIKE_CONFIRM) {
            BikeConfirmScreen(
                onBackHomeClick = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onOpenChat = { rideId ->
                    navController.navigate(Routes.createRideChatRoute(rideId))
                },
                onRateRide = { rideId ->
                    navController.navigate(Routes.createRideRatingRoute(rideId))
                }
            )
        }
        composable(
            route = Routes.RIDE_CHAT,
            arguments = listOf(
                navArgument("rideId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            RideChatScreen(
                rideId = backStackEntry.arguments?.getString("rideId").orEmpty(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.RIDE_RATING,
            arguments = listOf(
                navArgument("rideId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            RideRatingScreen(
                rideId = backStackEntry.arguments?.getString("rideId").orEmpty(),
                onBackClick = {
                    navController.popBackStack()
                },
                onDone = {
                    navController.navigate(Routes.RECENTLY_SERVICE) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.NOTIFICATION) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            NotificationScreen(
                currentRoute = currentRoute,
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.RECENTLY_SERVICE) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            HistoryScreen(
                currentRoute = currentRoute,
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onRateRide = { rideId ->
                    navController.navigate(Routes.createRideRatingRoute(rideId))
                }
            )
        }
        composable(Routes.PROFILE) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            ProfileScreen(
                currentRoute = currentRoute,
                onBottomNavClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPaymentClick = {
                    navController.navigate("add_payment") {
                        launchSingleTop = true
                    }
                },
                onNotificationClick = {
                    navController.navigate(Routes.NOTIFICATION) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.EXPRESS_PICKUP) {
            LocationPickerScreen(
                type = ExpressLocationType.PICKUP,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.EXPRESS_DROPOFF) {
            LocationPickerScreen(
                type = ExpressLocationType.DROPOFF,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
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
        composable("momo_payment") {
            MomoLinkScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun routeForRole(role: String): String {
    return when (role.uppercase()) {
        "ADMIN" -> Routes.ADMIN_DRIVERS
        "DRIVER" -> Routes.DRIVER_HOME
        else -> Routes.USER_HOME
    }
}
