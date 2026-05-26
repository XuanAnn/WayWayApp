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
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.example.waywayapp.ui.admin.driver.AdminDriverScreen
import com.example.waywayapp.ui.driver.home.DriverHomeScreen
import com.example.waywayapp.ui.auth.login.LoginScreen
import com.example.waywayapp.ui.auth.register.RegisterScreen
import com.example.waywayapp.ui.user.booking.BookingRoute
import com.example.waywayapp.ui.user.booking.express.model.ExpressLocationType
import com.example.waywayapp.ui.user.booking.express.picker.LocationPickerScreen
import com.example.waywayapp.ui.user.booking.food.FoodBookingScreen
import com.example.waywayapp.ui.user.booking.food.cart.CheckoutSuccessScreen
import com.example.waywayapp.ui.user.booking.food.cart.FoodCartScreen
import com.example.waywayapp.ui.user.booking.food.order.FoodOrderTrackingScreen
import com.example.waywayapp.ui.user.home.HomeScreen
import com.example.waywayapp.ui.user.notification.NotificationScreen
import com.example.waywayapp.ui.user.payment.AddPaymentScreen
import com.example.waywayapp.ui.user.profile.ProfileScreen
import com.example.waywayapp.ui.user.booking.bike.search.BikeSearchScreen
import com.example.waywayapp.ui.user.booking.bike.map.BikeLocationPickerScreen
import com.example.waywayapp.ui.user.booking.bike.confirm.BikeConfirmScreen
import com.example.waywayapp.ui.user.booking.bike.model.BikeLocationType
import com.example.waywayapp.ui.user.booking.car.confirm.CarConfirmScreen
import com.example.waywayapp.ui.user.booking.car.map.CarLocationPickerScreen
import com.example.waywayapp.ui.user.booking.car.model.CarLocationType
import com.example.waywayapp.ui.user.booking.car.search.CarSearchScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authRepository = FirebaseAuthRepository()
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser == null) {
            Routes.LOGIN
        } else {
            routeForRole(authRepository.getFastCurrentUserRole())
        }

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
            AdminDriverScreen()
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeScreen()
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
            BookingRoute(
                type = bookingType,
                onBackClick = {
                    navController.popBackStack()
                },
                onCartClick = {
                    navController.navigate(Routes.FOOD_CART)
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
                onCarPickupClick = {
                    navController.navigate(Routes.CAR_PICKUP_MAP)
                },
                onCarDropoffClick = {
                    navController.navigate(Routes.CAR_DROPOFF_MAP)
                },
                onCarConfirmClick = {
                    navController.navigate(Routes.CAR_CONFIRM)
                }
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
                },
                onPlaceOrderClick = {
                    navController.navigate(Routes.FOOD_ORDER_TRACKING)
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
        composable(Routes.CHECKOUT_SUCCESS) {
            CheckoutSuccessScreen(
                onBackHomeClick = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.FOOD_ORDER_TRACKING) {
            FoodOrderTrackingScreen(
                onBackHomeClick = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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
        composable(Routes.CAR_SEARCH) {
            CarSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPickupMapClick = {
                    navController.navigate(Routes.CAR_PICKUP_MAP)
                },
                onDropoffMapClick = {
                    navController.navigate(Routes.CAR_DROPOFF_MAP)
                },
                onConfirmClick = {
                    navController.navigate(Routes.CAR_CONFIRM)
                }
            )
        }

        composable(Routes.CAR_PICKUP_MAP) {
            CarLocationPickerScreen(
                type = CarLocationType.PICKUP,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CAR_DROPOFF_MAP) {
            CarLocationPickerScreen(
                type = CarLocationType.DROPOFF,
                onBackClick = {
                    navController.popBackStack()
                },
                onConfirmClick = {
                    navController.navigate(Routes.CAR_CONFIRM)
                }
            )
        }

        composable(Routes.CAR_CONFIRM) {
            CarConfirmScreen(
                onBackHomeClick = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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
