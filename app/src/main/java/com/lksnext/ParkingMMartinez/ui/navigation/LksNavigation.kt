package com.lksnext.ParkingMMartinez.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.LocalBookingRepository
import com.lksnext.ParkingMMartinez.data.repository.LocalUserRepository
import com.lksnext.ParkingMMartinez.data.repository.LocalVehicleRepository
import com.lksnext.ParkingMMartinez.ui.components.LksFooter
import com.lksnext.ParkingMMartinez.ui.screens.*
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.LoginViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.RecoveryViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.RegistrationViewModel

@Composable
fun LksNavigation() {
    val context = LocalContext.current

    // Instanciamos Repositorios y Managers
    val bookingRepository = LocalBookingRepository(context)
    val userRepository = LocalUserRepository(context)
    val vehicleRepository = LocalVehicleRepository(context)
    val session = SessionManager(context)

    val navController = rememberNavController()

    // --- FACTORY PARA LOS VIEWMODELS ---

    val loginViewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(userRepository, session) as T
            }
        }
    )

    val registrationViewModel: RegistrationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RegistrationViewModel(userRepository, vehicleRepository, session) as T
            }
        }
    )

    val recoveryViewModel: RecoveryViewModel = viewModel()

    val sharedBookingViewModel: BookingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookingViewModel(bookingRepository, session) as T
            }
        }
    )

    val registerViewModel: BookingRegisterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BookingRegisterViewModel(bookingRepository, session) as T
            }
        }
    )

    val mapViewModel: MapViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(bookingRepository) as T
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(vehicleRepository, userRepository, session) as T
            }
        }
    )

    // Lógica de inicio y estado de navegación
    val startDestination = if (session.isLoggedIn()) Screen.Map.route else Screen.Login.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showFooter = currentRoute != null &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.Register.route &&
            currentRoute != Screen.Recovery.route

    Scaffold(
        bottomBar = {
            if (showFooter) {
                LksFooter(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // --- LOGIN ---
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { shouldRemember ->
                        if (shouldRemember) session.saveSession(true)
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
                    viewModel = registrationViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // --- RECOVERY ---
            composable(Screen.Recovery.route) {
                RecoveryScreen(
                    viewModel = recoveryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- MAPA ---
            composable(Screen.Map.route) {
                MapScreen(
                    viewModel = mapViewModel,
                    onZoneClick = { zoneName ->
                        navController.navigate(Screen.Booking.createRoute(zoneName))
                    }
                )
            }

            // --- BOOKING ---
            composable(
                route = Screen.Booking.route,
                arguments = listOf(navArgument("zoneName") { type = NavType.StringType })
            ) { backStackEntry ->
                val zoneName = backStackEntry.arguments?.getString("zoneName") ?: "Standard Zone"
                BookingScreen(
                    viewModel = sharedBookingViewModel,
                    initialZone = zoneName,
                    onConfirmBooking = { navController.popBackStack() },
                    onManageVehicles = { navController.navigate(Screen.Profile.route) }
                )
            }

            // --- BOOKING LIST ---
            composable(Screen.BookingsList.route) {
                BookingRegisterScreen(
                    viewModel = registerViewModel,
                    bookingViewModel = sharedBookingViewModel,
                    onNavigateToEdit = { zoneName ->
                        navController.navigate(Screen.Booking.createRoute(zoneName))
                    }
                )
            }

            // --- PROFILE ---
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClick = {
                        session.clearSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // --- ALERTS ---
            composable(Screen.Alerts.route) { NotificationScreen() }
        }
    }
}