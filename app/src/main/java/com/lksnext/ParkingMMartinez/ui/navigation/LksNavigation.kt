package com.lksnext.ParkingMMartinez.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.lksnext.ParkingMMartinez.data.repository.FirebaseBookingRepository
import com.lksnext.ParkingMMartinez.data.repository.FirebaseUserRepository
import com.lksnext.ParkingMMartinez.data.repository.FirebaseVehicleRepository
import com.lksnext.ParkingMMartinez.ui.components.LksFooter
import com.lksnext.ParkingMMartinez.ui.screens.*
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.LoginViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.RecoveryViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.RegistrationViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.lksnext.ParkingMMartinez.data.repository.FirebaseNotificationRepository
import com.lksnext.ParkingMMartinez.ui.screens.NotificationScreen
import com.lksnext.ParkingMMartinez.ui.viewmodel.NotificationViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.SettingsViewModel

@Composable
fun LksNavigation() {
    val context = LocalContext.current

    val bookingRepository = FirebaseBookingRepository()
    val userRepository = FirebaseUserRepository()
    val vehicleRepository = FirebaseVehicleRepository()
    val notificationRepository = FirebaseNotificationRepository()
    val session = SessionManager(context)

    val navController = rememberNavController()

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
                BookingViewModel(bookingRepository, vehicleRepository, session, notificationRepository)
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
                MapViewModel(bookingRepository, notificationRepository, session)
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(ProfileViewModel::class) {
                ProfileViewModel(vehicleRepository, userRepository, bookingRepository, session)
            }
        }
    )

    val notificationViewModel: NotificationViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(NotificationViewModel::class) {
                NotificationViewModel(notificationRepository, session)
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            addInitializer(SettingsViewModel::class) {
                SettingsViewModel(userRepository, session)
            }
        }
    )


    val startDestination = if (session.isLoggedIn()) Screen.Map.route else Screen.Login.route

    LaunchedEffect(Unit) {
        if (session.isLoggedIn()) {
            session.getActiveUserId()?.let { userId ->
                notificationRepository.linkDeviceWithUser(userId)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showFooter = currentRoute != null &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.Register.route &&
            currentRoute != Screen.Recovery.route &&
            currentRoute != Screen.Settings.route &&
            currentRoute != Screen.Booking.route

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
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { shouldRemember ->
                        if (shouldRemember) session.saveSession(true)
                        session.getActiveUserId()?.let { userId ->
                            notificationRepository.linkDeviceWithUser(userId)
                        }
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToPasswordRecovery = { navController.navigate(Screen.Recovery.route) }
                )
            }

            composable(Screen.Register.route) {
                RegistrationScreen(
                    viewModel = registrationViewModel,
                    onRegisterSuccess = {
                        session.getActiveUserId()?.let { userId ->
                            notificationRepository.linkDeviceWithUser(userId)
                        }
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

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
                        // Sincronizamos la fecha completa justo antes de viajar para evitar el desajuste de meses
                        sharedBookingViewModel.onDateSelected(mapViewModel.selectedDate)

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
                val hour = backStackEntry.arguments?.getInt("hour") ?: 8
                val minute = backStackEntry.arguments?.getInt("minute") ?: 0

                BookingScreen(
                    viewModel = sharedBookingViewModel,
                    initialZone = zoneName,
                    //initialDay = day,
                    initialHour = hour,
                    initialMinute = minute,
                    onConfirmBooking = {
                        navController.popBackStack()
                        navController.navigate(Screen.BookingsList.route) {
                            launchSingleTop = true
                        }
                    },
                    onManageVehicles = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.BookingsList.route) {
                BookingRegisterScreen(
                    viewModel = registerViewModel,
                    bookingViewModel = sharedBookingViewModel,
                    onNavigateToEdit = { zoneName ->
                        navController.navigate(Screen.Booking.createRoute(zoneName))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClick = {
                        loginViewModel.resetLoginFields()
                        session.clearSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // --- ALERTS ---
            composable(Screen.Alerts.route) {
                NotificationScreen(viewModel = notificationViewModel)
            }

            // --- AJUSTES ---
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}