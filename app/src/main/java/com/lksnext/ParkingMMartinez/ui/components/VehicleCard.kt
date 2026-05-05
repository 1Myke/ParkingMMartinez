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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.model.VehicleType

@Composable
fun VehicleCard(
    name: String,
    plate: String,
    type: VehicleType,
    onDeleteClick: () -> Unit
) {
    // Definimos los colores dinámicos según el tipo
    val (containerColor, contentColor) = when (type) {
        VehicleType.STANDARD -> Color(0xFFFFF4E6) to Color(0xFFD9480F) // Naranja suave
        VehicleType.MOTORCYCLE -> Color(0xFFE7F5FF) to Color(0xFF1971C2) // Azul suave
        VehicleType.ELECTRIC -> Color(0xFFEBFBEE) to Color(0xFF2F9E44) // Verde suave
        VehicleType.ADAPTED -> Color(0xFFF1F3F5) to Color(0xFF495057) // Gris para vehiculos adaptados
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp) // Esquinas un poco más redondeadas
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del vehículo con fondo gris circular
            Surface(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFF1F3F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    // Cambiar el icono según el tipo también
                    imageVector = if (type == VehicleType.MOTORCYCLE) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = Color(0xFF495057)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // La matrícula con fondo gris suave para que destaque
                    Surface(
                        color = Color(0xFFF1F3F5),
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

                    // MEJORAS: El Badge con colores bonitos
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = type.toString().lowercase().capitalize(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick =  onDeleteClick ) {
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