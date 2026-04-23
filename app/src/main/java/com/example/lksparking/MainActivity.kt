package com.example.lksparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.lksparking.ui.screens.BookingScreen
import com.example.lksparking.ui.screens.MapScreen
import com.example.lksparking.ui.theme.LksParkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LksParkingTheme {
                // 1. Definimos la variable de estado para saber qué pantalla mostrar
                var currentScreen by remember { mutableStateOf("map") }

                // 2. El "conmutador" de pantallas
                when (currentScreen) {
                    "map" -> MapScreen(
                        onZoneClick = { currentScreen = "booking" }
                    )
                    "booking" -> BookingScreen()
                }
            }
        }
    }
}