package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.R


@Composable
fun ZoneCard(
    zone: ParkingZone,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, zone.color.copy(alpha = 0.5f)), // Borde del color de la zona
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo suave del mismo color
            Surface(
                color = zone.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = getIconForZone(zone.name), // Función que devuelva el icono
                    contentDescription = null,
                    tint = zone.color,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, fontWeight = FontWeight.Bold, color = zone.color)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${zone.availableSpots}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(" / ${zone.totalSpots} free", color = Color.Gray)
                }
            }

            // Etiqueta "AVAILABLE" de Figma
            Surface(
                color = Color(0xFF00C853),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    stringResource(R.string.label_available),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun getIconForZone(zoneName: String): ImageVector {
    return when {
        zoneName.contains("Standard", ignoreCase = true) -> Icons.Default.DirectionsCar
        zoneName.contains("EV", ignoreCase = true) || zoneName.contains("Charging", ignoreCase = true) -> Icons.Default.ElectricCar
        zoneName.contains("Disability", ignoreCase = true) || zoneName.contains("Accessible", ignoreCase = true) -> Icons.Default.Accessible
        zoneName.contains("Motorcycle", ignoreCase = true) -> Icons.Default.TwoWheeler
        else -> Icons.Default.LocalParking // Icono por defecto si no coincide ninguno
    }
}