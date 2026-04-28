package com.example.lksparking.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Recovery : Screen("recovery")
    object Map : Screen("map")
    object Profile : Screen("profile")
    // Esta ruta es especial: acepta un argumento
    object Booking : Screen("booking/{zoneName}") {
        fun createRoute(zoneName: String) = "booking/$zoneName"
    }
    object BookingsList: Screen("bookings_lista")
    object Alerts: Screen("alert")
}