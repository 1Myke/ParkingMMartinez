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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

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
        factory = viewModelFactory {
            addInitializer(LoginViewModel::class) {
                LoginViewModel(userRepository, session)
            }
        }
    )

    val registrationViewModel: RegistrationViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(RegistrationViewModel::class) {
                RegistrationViewModel(userRepository, vehicleRepository, session)
            }
        }
    )

    val recoveryViewModel: RecoveryViewModel = viewModel()

    val sharedBookingViewModel: BookingViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(BookingViewModel::class) {
                BookingViewModel(bookingRepository, session)
            }
        }
    )

    val registerViewModel: BookingRegisterViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(BookingRegisterViewModel::class) {
                BookingRegisterViewModel(bookingRepository, session)
            }
        }
    )

    val mapViewModel: MapViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(MapViewModel::class) {
                MapViewModel(bookingRepository)
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(ProfileViewModel::class) {
                ProfileViewModel(vehicleRepository, userRepository, session)
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
                    bookingViewModel = sharedBookingViewModel,
                    onZoneClick = { zoneName ->
                        navController.navigate(
                            Screen.Booking.createRoute(
                                zoneName = zoneName,
                                day = mapViewModel.selectedDayNumber,
                                hour = mapViewModel.selectedStartTime.hour,
                                minute = mapViewModel.selectedStartTime.minute
                            )
                        )
                    }
                )
            }

            // --- BOOKING ---
            composable(
                route = Screen.Booking.route,
                arguments = listOf(
                    navArgument("zoneName") { type = NavType.StringType },
                    navArgument("day") { type = NavType.IntType },
                    navArgument("hour") { type = NavType.IntType },
                    navArgument("minute") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val zoneName = backStackEntry.arguments?.getString("zoneName") ?: "Standard Zone"
                val day = backStackEntry.arguments?.getInt("day") ?: 0
                val hour = backStackEntry.arguments?.getInt("hour") ?: 8
                val minute = backStackEntry.arguments?.getInt("minute") ?: 0

                BookingScreen(
                    viewModel = sharedBookingViewModel,
                    initialZone = zoneName,
                    initialDay = day,
                    initialHour = hour,
                    initialMinute = minute,
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