package com.lksnext.ParkingMMartinez.model

import androidx.compose.ui.graphics.Color

data class ParkingZone(
    val name: String,
    val availableSpots: Int,
    val totalSpots: Int,
    val iconRes: Int, // Para el icono de los vehiculos
    val color: Color
)
