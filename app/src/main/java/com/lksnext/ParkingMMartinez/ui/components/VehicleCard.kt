package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R // 🌐 Importamos R para resolver los strings traducidos
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.screens.getVehicleTypeDisplayNameRes // 🌐 Importamos el helper del ProfileScreen
import com.lksnext.ParkingMMartinez.ui.theme.*

@Composable
fun VehicleCard(
    name: String,
    plate: String,
    type: VehicleType,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    deleteButtonModifier: Modifier = Modifier
) {
    // Definimos los colores dinámicos según el tipo
    val (containerColor, contentColor) = when (type) {
        VehicleType.STANDARD -> standardVehicleColor
        VehicleType.MOTORCYCLE -> motorcycleVehicleColor
        VehicleType.ELECTRIC -> electricVehicleColor
        VehicleType.ADAPTED -> adaptedVehicleColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del vehículo con fondo gris circular
            Surface(
                modifier = Modifier.size(48.dp),
                color = mistGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    // 🚗 MEJORA: Cambiamos el icono dinámicamente según TODOS los tipos reales
                    imageVector = when (type) {
                        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
                        VehicleType.ELECTRIC -> Icons.Default.ElectricCar
                        VehicleType.ADAPTED -> Icons.AutoMirrored.Filled.Accessible
                        else -> Icons.Default.DirectionsCar
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = grisPizarra
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // La matrícula con fondo gris suave para que destaque
                    Surface(
                        color = mistGray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = plate,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // El Badge con colores bonitos e idioma corregido
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            // 🌐 SOLUCIÓN: Usamos stringResource pasándole el mapeador dinámico del Enum
                            text = stringResource(id = getVehicleTypeDisplayNameRes(type)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick , modifier = deleteButtonModifier) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete vehicle",
                        tint = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}