package com.example.smartwastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartwastemanagementapp.navigation.Screen
import com.example.smartwastemanagementapp.ui.screens.*
import com.example.smartwastemanagementapp.ui.theme.SmartWasteManagementAppTheme
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartWasteManagementAppTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                // Start directly at Login or Home to bypass splash screen hangs
                val startDestination = if (authViewModel.isLoggedIn.value) Screen.Home.route else Screen.Login.route

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Login.route) {

                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                        )
                    }
                    composable(Screen.Signup.route) {
                        SignupScreen(
                            onSignupSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Signup.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                        )
                    }
                    composable(Screen.Home.route) {
                        val authViewModel: AuthViewModel = viewModel()
                        HomeScreen(
                            onReportWaste = { navController.navigate(Screen.ReportWaste.route) },
                            onViewReports = { navController.navigate(Screen.ViewReports.route) },
                            onViewMap = { navController.navigate(Screen.Map.route) },
                            onLogout = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            authViewModel = authViewModel
                        )
                    }
                    composable(Screen.ReportWaste.route) {
                        val wasteViewModel: WasteViewModel = viewModel()
                        ReportWasteScreen(
                            onSuccess = { navController.popBackStack() },
                            onBack = { navController.popBackStack() },
                            viewModel = wasteViewModel
                        )
                    }
                    composable(Screen.ViewReports.route) {
                        val wasteViewModel: WasteViewModel = viewModel()
                        ViewReportsScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = wasteViewModel
                        )
                    }
                    composable(Screen.Map.route) {
                        val wasteViewModel: WasteViewModel = viewModel()
                        MapScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = wasteViewModel
                        )
                    }
                }
            }
        }
    }
}