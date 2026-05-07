package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTimePicker
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType

@Composable
fun BookingScreen(
    viewModel: BookingViewModel = viewModel(),
    initialZone: String = "",
    onConfirmBooking: () -> Unit = {},
    onManageVehicles: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isValidTime = viewModel.isDateTimeValid()

    LaunchedEffect(Unit) {
        viewModel.checkUserReservationStatus(context)
        viewModel.setZone(initialZone)
        viewModel.loadAndFilterVehicles(context)
    }

    // Dialogo del timepicker
    if (viewModel.showTimePicker) {
        LksTimePicker(
            onConfirm = { h, m ->
                viewModel.onTimeChange(h, m) // Actualiza la hora en el ViewModel
                viewModel.onShowTimePickerChange(false) // Cierra el diálogo
            },
            onDismiss = {
                viewModel.onShowTimePickerChange(false) // Cierra el diálogo si cancelan
            }
        )
    }

    LaunchedEffect(initialZone) {
        viewModel.setZone(initialZone)
    }

    val vehicleIcon = when (viewModel.selectedVehicle?.type) {
        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
        VehicleType.ELECTRIC -> Icons.Default.ElectricCar
        VehicleType.ADAPTED -> Icons.Default.Accessible
        else -> Icons.Default.DirectionsCar
    }

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
                        text = viewModel.parkingZone,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {

            // 2. SELECCIÓN DE VEHÍCULO
            SectionHeader(
                title = "Select Vehicle",
                actionText = "Manage",
                onActionClick = onManageVehicles
            ) //MEJORAS: CUANDO HAMOS CLICK EN EL TEXTO MANAGE NOS TIENE QUE LLEVAR AL PERFIL
            //MEJORAS: SI HAY MAS DE UN VEHICULO DE LA MISMA CLASE NOS TIENE QUE DAR LA OPCION PARA SELECCIONAR EL QUE QUERAMOS
            // POR DEFECTO SIEMPRE NOS VA A PONER EL PRIMERO QUE ENCUENTRE EN LA LISTA
            if (viewModel.selectedVehicle != null) {
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
                        Icon(vehicleIcon, null, tint = LksOrange)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(viewModel.selectedVehicle!!.name, fontWeight = FontWeight.Bold)
                            Text(viewModel.selectedVehicle!!.plate, color = Color.Gray)
                        }
                    }
                }
            } else { // En caso de que no tenga vehiculo compatible
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)), // Fondo rojizo suave
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "No compatible vehicles found for ${viewModel.parkingZone}.",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Please add a vehicle of this type in 'Manage' to continue.",
                            style = MaterialTheme.typography.bodySmall
                        )
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
                        isSelected = viewModel.selectedDay == day,
                        onClick = { viewModel.onDateSelected(day) }
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

                    OutlinedCard(
                        onClick = { viewModel.onShowTimePickerChange(true) }, // ABRIMOS EL RELOJ
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
                                String.format("%02d:%02d", viewModel.startHour, viewModel.startMinute),
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ENDING AT ${viewModel.getEndTime()}",
                                color = LksOrange,
                                fontWeight = FontWeight.Bold
                            )

                            // Si el ViewModel dice que es el día siguiente, añadimos el aviso
                            if (viewModel.isNextDay()) {
                                Text(
                                    text = " (+1 day)",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // Área de Duración
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
                                    text = viewModel.duration.toInt().toString(),
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
                            //MEJORAS: USAR EL TIMESLIDER QUE TENGO EN COMPONENTES
                            Slider(
                                value = viewModel.duration,
                                onValueChange = { viewModel.onDurationChange(it) },
                                valueRange = 1f..8f,
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.hasActiveReservation) {
                    Text(
                        text = "You already have an active reservation.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else if (!isValidTime) {
                    // Si no tiene reserva pero la hora es pasada
                    Text(
                        text = "Cannot book in the past. Select a future time.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LksButton(
                    text = "Confirm Reservation",
                    // Habilitado solo si no hay reserva activa Y hay un vehículo compatible
                    enabled = !viewModel.hasActiveReservation && viewModel.selectedVehicle != null && isValidTime,
                    onClick = {
                        val realZone = ParkingMock.zones.find { it.name == viewModel.parkingZone }
                            ?: ParkingMock.zones.first()

                        // Usamos el vehículo que el ViewModel ha filtrado
                        viewModel.selectedVehicle?.let { vehicle ->
                            viewModel.confirmReservation(context, vehicle, realZone) {
                                onConfirmBooking()
                            }
                        }
                    }
                )

            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {} // Nuevo parámetro
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        if (actionText != null) {
            Text(
                text = actionText,
                color = LksOrange,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp).clickable { onActionClick() } // CLICABLE
            )
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