package com.example.lksparking.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lksparking.model.ParkingZone
import com.example.lksparking.ui.components.LksHeader
import com.example.lksparking.ui.components.ZoneCard

@Composable
fun MapScreen(onZoneClick: () -> Unit) {
    val zones = listOf(
        ParkingZone("Standard", 10, 24, 0, Color.Gray),
        ParkingZone("EV Charging", 2, 4, 0, Color.Green),
        ParkingZone("Disability Zone", 4, 6, 0, Color.Blue),
        ParkingZone("Motorcycle", 11, 16, 0, Color(0xFFA66FB5))
    )

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        LksHeader(
            title = "LKS Next Parking",
            subtitle = "Select a zone to book a spot"
        )

        Column(modifier = Modifier.padding(16.dp)) {
            zones.forEach { zone ->
                ZoneCard(
                    zone = zone,
                    onClick = { onZoneClick() }
                )
            }
        }
    }
}