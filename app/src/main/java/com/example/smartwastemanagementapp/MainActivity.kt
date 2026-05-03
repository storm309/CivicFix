package com.example.smartwastemanagementapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartwastemanagementapp.navigation.Screen
import com.example.smartwastemanagementapp.ui.screens.*
import com.example.smartwastemanagementapp.ui.theme.SmartWasteManagementAppTheme
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.example.smartwastemanagementapp.util.LanguageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.applySavedLanguage(this)
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
                                    navController.navigate(Screen.CompleteProfile.route) { 
                                        popUpTo(Screen.Login.route) { inclusive = true } 
                                    }
                                } else {
                                    val dest = if (authViewModel.isAdmin.value) Screen.AdminDashboard.route else Screen.Home.route
                                    navController.navigate(dest) { 
                                        popUpTo(Screen.Login.route) { inclusive = true } 
                                    }
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
                            isEditMode = false,
                            onComplete = {
                                navController.navigate(Screen.Home.route) { popUpTo(0) }
                            }
                        )
                    }
                    composable(Screen.EditProfile.route) {
                        CompleteProfileScreen(
                            viewModel = authViewModel,
                            isEditMode = true,
                            onComplete = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onReportWaste = { navController.navigate(Screen.ReportWaste.route) },
                            onViewReports = { navController.navigate(Screen.ViewReports.route) },
                            onViewMap = { navController.navigate(Screen.Map.route) },
                            onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            },
                            onAdminDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                            authViewModel = authViewModel
                        )
                    }
                    composable(Screen.AdminDashboard.route) {
                        if (!authViewModel.isAdmin.value) {
                            LaunchedEffect(Unit) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                                }
                            }
                            return@composable
                        }
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
                            viewModel = wasteViewModel,
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
                            viewModel = wasteViewModel,
                            authViewModel = authViewModel
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
