package com.example.waywayapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.waywayapp.ui.auth.login.LoginScreen
import com.example.waywayapp.ui.auth.register.RegisterScreen
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
        composable(Routes.BOOKING) {

        }
        // Placeholder for Home
        composable(Routes.USER_HOME) {
            // Your Home Screen
            HomeScreen()
        }
    }
}
