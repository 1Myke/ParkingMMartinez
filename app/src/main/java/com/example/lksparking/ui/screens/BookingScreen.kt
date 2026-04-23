package com.example.lksparking.ui.screens

import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.lksparking.ui.components.TimePickerField
import com.example.lksparking.ui.components.TimeSlider
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun BookingScreen() {
    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(1f) }
    var selectedVehicle by remember { mutableStateOf("Car")}

    val context = LocalContext.current
    val picker = TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("START TIME")
        TimePickerField(
            startTime = String.format("%02d:%02d", startHour, startMinute),
            onOpenPicker = { picker.show() }
        )

        Spacer(Modifier.height(24.dp))

        Text("DURATION")
        TimeSlider(
            currentHours = duration,
            onHoursChange = { duration = it }
        )

        val totalHours = startHour + duration.toInt()
        val endHour = totalHours % 24
        val isNextDay = totalHours >= 24

        Text(
            text = "ENDING AT ${String.format("%02d:%02d", endHour, startMinute)}",
            color = LksOrange,
            style = MaterialTheme.typography.headlineMedium
        )

        if (isNextDay){
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(+1 day)",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("SELECT YOUR VEHICLE", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedVehicle = "Car" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedVehicle == "Car") LksOrange else Color.Gray
                )
            ) { Text("🚗 Car") }

            Button(
                onClick = { selectedVehicle = "Moto" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedVehicle == "Moto") LksOrange else Color.Gray
                )
            ) { Text("🏍️ Moto") }
        }

        Button(
            onClick = {
                println("RESERVA: $selectedVehicle a las $startHour:$startMinute")
            },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LksOrange)
        ) {
            Text("CONFIRM BOOKING", color = Color.White)
        }
    }
}
