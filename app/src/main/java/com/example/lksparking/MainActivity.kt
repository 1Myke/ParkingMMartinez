package com.example.lksparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.lksparking.ui.screens.BookingScreen
import com.example.lksparking.ui.screens.LoginScreen
import com.example.lksparking.ui.screens.RegistrationScreen
import com.example.lksparking.ui.screens.MapScreen
import com.example.lksparking.ui.screens.RecoveryScreen
import com.example.lksparking.ui.theme.LksParkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LksParkingTheme {
                // 1. Definimos la variable que controla qué pantalla se ve
                // Valores posibles: "login", "register", "map"
                var currentScreen by remember { mutableStateOf("login") }

                // 2. El "Selector" de pantallas
                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = {
                            // Si el login es correcto, vamos al mapa
                            currentScreen = "map"
                        },
                        onNavigateToRegister = {
                            // Si pulsa "Sign Up", vamos a registro
                            currentScreen = "register"
                        },
                        onNavigateToPasswordRecovery = {
                            currentScreen = "recovery"
                        }
                    )

                    "recovery" -> RecoveryScreen(
                        onNavigateBack = {
                            currentScreen = "login"
                        }
                    )

                    "register" -> RegistrationScreen(
                        onRegisterSuccess = {
                            // Si se registra bien, le mandamos al login para que entre
                            currentScreen = "login"
                        },
                        onNavigateToLogin = {
                            // Si pulsa "Ya tengo cuenta", vuelve a login
                            currentScreen = "login"
                        }
                    )

                    "map" -> MapScreen(
                        onZoneClick = {
                            currentScreen = "booking"
                        }
                    )

                    "booking" -> BookingScreen(
                        initialZone = "Super Vip zone"
                    )
                }
            }
        }
    }
}