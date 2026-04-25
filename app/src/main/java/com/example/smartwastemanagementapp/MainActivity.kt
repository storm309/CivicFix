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
                
                NavHost(navController = navController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onTimeout = {
                                val isLoggedIn = authViewModel.isLoggedIn.value
                                val isComplete = authViewModel.isProfileComplete.value
                                
                                if (!isLoggedIn) {
                                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                                } else if (!isComplete) {
                                    navController.navigate(Screen.CompleteProfile.route) { popUpTo(0) }
                                } else {
                                    val dest = if (authViewModel.isAdmin.value) Screen.AdminDashboard.route else Screen.Home.route
                                    navController.navigate(dest) { popUpTo(0) }
                                }
                            }
                        )
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                if (!authViewModel.isProfileComplete.value) {
                                    navController.navigate(Screen.CompleteProfile.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                                } else {
                                    val dest = if (authViewModel.isAdmin.value) Screen.AdminDashboard.route else Screen.Home.route
                                    navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                                }
                            },
                            onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
                        )
                    }
                    composable(Screen.Signup.route) {
                        SignupScreen(
                            viewModel = authViewModel,
                            onSignupSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Signup.route) { inclusive = true }
                                }
                            },
                            onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                        )
                    }
                    composable(Screen.CompleteProfile.route) {
                        CompleteProfileScreen(
                            viewModel = authViewModel,
                            onComplete = {
                                navController.navigate(Screen.Home.route) { popUpTo(0) }
                            }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onReportWaste = { navController.navigate(Screen.ReportWaste.route) },
                            onViewReports = { navController.navigate(Screen.ViewReports.route) },
                            onViewMap = { navController.navigate(Screen.Map.route) },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            },
                            authViewModel = authViewModel
                        )
                    }
                    composable(Screen.AdminDashboard.route) {
                        val wasteViewModel: WasteViewModel = viewModel()
                        AdminDashboardScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            },
                            onBackToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.AdminDashboard.route)
                                }
                            },
                            viewModel = wasteViewModel
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
