package com.example.lksparking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lksparking.ui.navigation.LksNavigation
import com.example.lksparking.ui.theme.LksParkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LksParkingTheme {
                LksNavigation()
            }
        }
    }
}