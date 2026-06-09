package com.example.waywayapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.waywayapp.ui.navigation.AppNavHost
import com.example.waywayapp.ui.user.booking.bike.BikeSharedViewModel
import com.example.waywayapp.ui.theme.WayWayAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleMomoReturnIntent(intent)
        enableEdgeToEdge()
        setContent {
            WayWayAppTheme {

                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleMomoReturnIntent(intent)
    }

    private fun handleMomoReturnIntent(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme != "wayway" || data.host != "momo-return") return

        BikeSharedViewModel.viewModel.handleMomoReturn(
            orderId = data.getQueryParameter("orderId"),
            resultCode = data.getQueryParameter("resultCode"),
            message = data.getQueryParameter("message")
        )
    }
}
