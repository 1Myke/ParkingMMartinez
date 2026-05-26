package com.lksnext.ParkingMMartinez.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Recovery : Screen("recovery")
    object Map : Screen("map")
    object Profile : Screen("profile")
    // Esta ruta es especial: acepta un argumento
    object Booking : Screen("booking/{zoneName}/{day}/{hour}/{minute}") {
        fun createRoute(zoneName: String, day: Int = -1, hour: Int = -1, minute: Int = -1) =
            "booking/$zoneName/$day/$hour/$minute"
    }
    object BookingsList: Screen("bookings_lista")
    object Alerts: Screen("alert")
}