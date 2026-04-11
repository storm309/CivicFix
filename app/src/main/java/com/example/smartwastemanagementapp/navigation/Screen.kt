package com.example.smartwastemanagementapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object ReportWaste : Screen("report_waste")
    object ViewReports : Screen("view_reports")
    object Map : Screen("map")
}
