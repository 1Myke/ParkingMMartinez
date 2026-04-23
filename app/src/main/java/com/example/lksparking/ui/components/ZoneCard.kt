package com.example.lksparking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lksparking.model.ParkingZone


@Composable
fun ZoneCard(zone: ParkingZone, onClick: () -> Unit){
    val isFull = zone.availableSpots == 0
    val stateText = if (isFull) "OCCUPIED" else "AVAILABLE"
    val stateColor = if (isFull) Color.Red else Color.Green
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable{onClick()},
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, zone.categoryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Icon",
                style = MaterialTheme.typography.headlineLarge
            )

            Column(
                modifier = Modifier
                    .padding(start=16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = zone.categoryColor
                )
                Text(
                    text = "${zone.availableSpots} / ${zone.totalSpots} free",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stateText,
                style = MaterialTheme.typography.labelLarge,
                color = stateColor
            )
        }
    }
}