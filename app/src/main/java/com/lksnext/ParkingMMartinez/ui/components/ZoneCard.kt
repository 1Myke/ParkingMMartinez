package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
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
import com.lksnext.ParkingMMartinez.ui.theme.LksGreen
import com.lksnext.ParkingMMartinez.ui.theme.verdePino

@Composable
fun ZoneCard(
    zone: ParkingZone,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isZoneFull = zone.availableSpots <= 0

    OutlinedCard(
        onClick = { if (!isZoneFull) onClick() },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, getBorderColor(isZoneFull, zone.color)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isZoneFull) Color(0xFFF9F9F9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZoneIconSection(zoneName = zone.name, zoneColor = zone.color, isZoneFull = isZoneFull)

            Spacer(Modifier.width(16.dp))

            ZoneInfoSection(zone = zone, isZoneFull = isZoneFull, modifier = Modifier.weight(1f))

            ZoneBadgeSection(isZoneFull = isZoneFull)
        }
    }
}

// --- SUB-COMPONENTES ---

@Composable
private fun ZoneIconSection(zoneName: String, zoneColor: Color, isZoneFull: Boolean) {
    val backgroundColor = if (isZoneFull) Color.LightGray.copy(alpha = 0.2f) else zoneColor.copy(alpha = 0.1f)
    val iconColor = if (isZoneFull) Color.Gray else zoneColor

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = getIconForZone(zoneName),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ZoneInfoSection(zone: ParkingZone, isZoneFull: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = zone.name,
            fontWeight = FontWeight.Bold,
            color = if (isZoneFull) Color.Gray else zone.color
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${zone.availableSpots}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isZoneFull) Color.Red else verdePino
            )
            Text(" / ${zone.totalSpots} free", color = Color.Gray)
        }
    }
}

@Composable
private fun ZoneBadgeSection(isZoneFull: Boolean) {
    Surface(
        color = if (isZoneFull) Color(0xFFE53935) else LksGreen,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (isZoneFull) "FULL" else stringResource(R.string.label_available),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- FUNCIONES PURAS DE LÓGICA ---

private fun getBorderColor(isZoneFull: Boolean, zoneColor: Color): Color {
    return if (isZoneFull) Color.LightGray else zoneColor.copy(alpha = 0.5f)
}

@Composable
fun getIconForZone(zoneName: String): ImageVector {
    return when {
        zoneName.contains("Standard", ignoreCase = true) -> Icons.Default.DirectionsCar
        zoneName.contains("EV", ignoreCase = true) || zoneName.contains("Charging", ignoreCase = true) -> Icons.Default.ElectricCar
        zoneName.contains("Disability", ignoreCase = true) || zoneName.contains("Accessible", ignoreCase = true) -> Icons.AutoMirrored.Default.Accessible
        zoneName.contains("Motorcycle", ignoreCase = true) -> Icons.Default.TwoWheeler
        else -> Icons.Default.LocalParking
    }
}