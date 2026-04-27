package com.example.lksparking.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lksparking.ui.components.LksButton
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun BookingScreen(
    onConfirmBooking: () -> Unit = {}
) {
    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(4f) }
    var selectedDate by remember { mutableStateOf(26) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. HEADER (Zona de Parking)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LksOrange,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "YOU ARE BOOKING A SPOT IN",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "Standard Zone",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {

            // 2. SELECCIÓN DE VEHÍCULO
            SectionHeader(title = "Select Vehicle", actionText = "Manage")
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                border = BorderStroke(2.dp, LksOrange),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LksOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = LksOrange,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("My Car", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("1234 ABC", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. SELECT DATE (Carrusel dinámico de 8 días)
            SectionHeader(title = "Select Date")

            // Necesitamos importar java.time.* para manejar fechas fácilmente
            // Si te da error, asegúrate de tener habilitado "desugaring" o usa Calendar
            val calendar = java.util.Calendar.getInstance()
            val dates = (0..7).map { offset ->
                val tempCal = calendar.clone() as java.util.Calendar
                tempCal.add(java.util.Calendar.DAY_OF_YEAR, offset)

                val dayNum = tempCal.get(java.util.Calendar.DAY_OF_MONTH)
                val dayName = when (offset) {
                    0 -> "TODAY"
                    else -> java.text.SimpleDateFormat("EEE", java.util.Locale.ENGLISH)
                        .format(tempCal.time).uppercase()
                }
                dayNum to dayName
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()), // Añadimos scroll para que quepan los 8
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dates.forEach { (day, label) ->
                    DateItem(
                        day = day.toString(),
                        label = label,
                        isSelected = selectedDate == day,
                        onClick = { selectedDate = day }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 4. TIME & DURATION
            SectionHeader(title = "Time & Duration")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("START TIME", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    // Simulador de Time Picker Field
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                String.format("%02d:00", startHour),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("DURATION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            "ENDING AT ${String.format("%02d:00", (startHour + duration.toInt()) % 24)}",
                            color = LksOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Área de Duración estilo Premium
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(Color(0xFFFFF8F1), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = duration.toInt().toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = LksOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    " hours",
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    color = LksOrange,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Slider(
                                value = duration,
                                onValueChange = { duration = it },
                                valueRange = 1f..12f,
                                colors = SliderDefaults.colors(
                                    thumbColor = LksOrange,
                                    activeTrackColor = LksOrange,
                                    inactiveTrackColor = LksOrange.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // 5. CONFIRM BUTTON
            LksButton(
                text = "Confirm Reservation",
                onClick = { onConfirmBooking() }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono opcional aquí si quieres
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        if (actionText != null) {
            Text(actionText, color = LksOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DateItem(day: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) LksOrange else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.size(width = 62.dp, height = 75.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 10.sp, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
            Text(day, fontSize = 20.sp, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BookingScreenPreview() {
    BookingScreen()
}