package com.example.vvusa_jc.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vvusa_jc.ui.auth.LoginScreen
import com.example.vvusa_jc.ui.auth.RegisterScreen
import com.example.vvusa_jc.ui.auth.AuthViewModel
import com.example.vvusa_jc.ui.home.HomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vvusa_jc.ui.cafeteria.CafeteriaScreen
import com.example.vvusa_jc.ui.hostel.HostelScreen
import com.example.vvusa_jc.ui.hostel.HostelViewModel
import com.example.vvusa_jc.ui.market.MarketScreen
import com.example.vvusa_jc.ui.workstudy.WorkstudyScreen

object Routes {
    // Auth routes
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Main app routes
    const val HOME = "home"
    const val CAFETERIA = "cafeteria"
    const val HOSTEL = "hostel"
    const val WORKSTUDY = "workstudy"
    const val MARKETPLACE = "marketplace"
    const val PROFILE = "profile"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val hostelViewModel: HostelViewModel = viewModel()

    Scaffold(
        bottomBar = { VVUSABottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }},
                    onForgotPassword = { /* Skip for now */ }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }}
                )
            }

            // Main app screens
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
                )
            }

            composable(Routes.CAFETERIA) {
                CafeteriaScreen()
            }

            composable(Routes.HOSTEL) {
                HostelScreen(
                    viewModel = hostelViewModel,
                    onRoomSelection = { hostelName, floorName, roomNumber ->
                        // Handle room selection
                        // You can log the selection or navigate to a confirmation screen
                        Log.d("HostelBooking", "Selected room: $roomNumber in $hostelName, floor $floorName")
                    },
                    onPayment = { hostelName, floorName, roomNumber, price ->
                        // Handle payment process
                        // You might navigate to a payment screen or launch a payment gateway
                        Log.d("HostelBooking", "Payment for room: $roomNumber in $hostelName, floor $floorName, price: $price")
                        // Example: navController.navigate("payment_screen/$hostelName/$roomNumber/$price")
                    },
                    onViewStatus = {
                        // Navigate to room status screen
                        hostelViewModel.loadUserData()
                        Log.d("HostelBooking", "Viewing room status")
                        // Example: navController.navigate(Routes.ROOM_STATUS)
                    }
                )
            }

            composable(Routes.WORKSTUDY) {
                WorkstudyScreen()
            }

            composable(Routes.MARKETPLACE) {
                MarketScreen(
                    onNavigateToChat = { sellerId ->
                        // You can implement this later for chat functionality
                        Log.d("MarketScreen", "Navigate to chat with seller: $sellerId")
                    },
                    onNavigateToProductDetail = { productId ->
                        // Navigate to product detail screen (to be implemented)
                        Log.d("MarketScreen", "Navigate to product: $productId")
                    }
                )
            }

            composable(Routes.PROFILE) {
                PlaceholderScreen(text = "Profile Screen")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}