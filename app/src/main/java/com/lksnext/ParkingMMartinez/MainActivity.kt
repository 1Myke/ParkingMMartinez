package com.lksnext.ParkingMMartinez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lksnext.ParkingMMartinez.ui.navigation.LksNavigation
import com.lksnext.ParkingMMartinez.ui.theme.LksParkingTheme

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