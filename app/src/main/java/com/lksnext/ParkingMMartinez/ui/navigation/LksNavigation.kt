package com.lksnext.ParkingMMartinez.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.ui.components.LksFooter
import com.lksnext.ParkingMMartinez.ui.screens.*
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel

@Composable
fun LksNavigation() {

    //Verficiar si ya esta loggeado el usuario
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val startDestination = if (sessionManager.isLoggedIn()) Screen.Map.route
    else Screen.Login.route

    val navController = rememberNavController()

    // Observamos en qué pantalla estamos actualmente
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definimos dónde NO queremos que aparezca el footer (Login, Register, Recovery)
    val showFooter = currentRoute != null &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.Register.route &&
            currentRoute != Screen.Recovery.route

    val sharedBookingViewModel: BookingViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (showFooter) {
                // Pasamos el navController para que el footer pueda ejecutar .navigate()
                LksFooter(navController = navController)
            }
        }
    ) { paddingValues ->
        // El paddingValues es vital: evita que el contenido quede debajo del footer
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // --- LOGIN ---
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { shouldRemember ->
                        if (shouldRemember) {
                            sessionManager.saveSession(true)
                        }
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToPasswordRecovery = { navController.navigate(Screen.Recovery.route) }
                )
            }

            // --- REGISTER ---
            composable(Screen.Register.route) {
                RegistrationScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Map.route) },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // --- RECOVERY ---
            composable(Screen.Recovery.route) {
                RecoveryScreen(onNavigateBack = { navController.popBackStack() })
            }

            // --- MAPA (PANTALLA PRINCIPAL) ---
            composable(Screen.Map.route) {
                MapScreen(onZoneClick = { zoneName ->
                    navController.navigate(Screen.Booking.createRoute(zoneName))
                })
            }

            /*
            // --- BOOKING (PANTALLA DE DETALLE) ---
            composable(
                route = Screen.Booking.route,
                arguments = listOf(navArgument("zoneName") { type = NavType.StringType })
            ) { backStackEntry ->
                val zoneName = backStackEntry.arguments?.getString("zoneName") ?: "Standard Zone"
                BookingScreen(
                    initialZone = zoneName,
                    onConfirmBooking = { navController.popBackStack() },
                    onManageVehicles = { navController.navigate(Screen.Profile.route) }
                )
            }

            // --- BOOKING RESERVATION ---
            composable(Screen.BookingsList.route) {
                BookingRegisterScreen(
                    onNavigateToEdit = { zoneName ->
                        navController.navigate(Screen.Booking.createRoute(zoneName))
                    }
                )
            }
             */

            // --- BOOKING (La pantalla de reservar) ---
            composable(
                route = Screen.Booking.route,
                arguments = listOf(navArgument("zoneName") { type = NavType.StringType })
            ) { backStackEntry ->
                val zoneName = backStackEntry.arguments?.getString("zoneName") ?: "Standard Zone"
                BookingScreen(
                    viewModel = sharedBookingViewModel, // <--- USAMOS EL COMPARTIDO
                    initialZone = zoneName,
                    onConfirmBooking = { navController.popBackStack() },
                    onManageVehicles = { navController.navigate(Screen.Profile.route) }
                )
            }

            // --- BOOKING REGISTER (La lista) ---
            composable(Screen.BookingsList.route) {
                BookingRegisterScreen(
                    bookingViewModel = sharedBookingViewModel, // <--- USAMOS EL MISMO AQUÍ
                    onNavigateToEdit = { zoneName ->
                        navController.navigate(Screen.Booking.createRoute(zoneName))
                    }
                )
            }

            // --- PROFILE (PANTALLA PRINCIPAL) ---
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogoutClick = {
                        sessionManager.clearSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // MEJORA: --- PRÓXIMAMENTE: ALERTS Y BOOKINGS LIST ---
            composable(Screen.Alerts.route) { NotificationScreen() }
        }
    }
}