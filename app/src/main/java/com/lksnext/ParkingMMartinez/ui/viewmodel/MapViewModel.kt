package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.model.ParkingZone

class MapViewModel : ViewModel() {
    // Lista de zonas (luego vendrá de una base de datos)
    private val _zones = mutableStateListOf(
        ParkingZone("Disability Zone", 4, 6, 0, Color(0xFF2D5AF0)), // Azul Figma
        ParkingZone("EV Charging Zone", 3, 4, 0, Color(0xFF00C853)), // Verde Figma
        ParkingZone("Standard Zone", 16, 24, 0, Color(0xFF455A64)),  // Gris Figma
        ParkingZone("Motorcycle Zone", 11, 16, 0, Color(0xFFA66FB5))
    )
    val zones: List<ParkingZone> get() = _zones
}