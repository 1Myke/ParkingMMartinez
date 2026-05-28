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
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTimePicker
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.DateItem
import com.lksnext.ParkingMMartinez.ui.theme.lightGray
import com.lksnext.ParkingMMartinez.ui.theme.bookingCardColor
import com.lksnext.ParkingMMartinez.ui.theme.cremaSuave
import com.lksnext.ParkingMMartinez.ui.theme.palePink

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    initialZone: String = "",
    initialDay: Int = 0,
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onConfirmBooking: () -> Unit = {},
    onManageVehicles: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val todayStr = stringResource(R.string.booking_today)

    val isButtonEnabled = viewModel.isButtonEnabled

    LaunchedEffect(initialZone, initialDay, initialHour, initialMinute) {
        viewModel.setZone(initialZone)

        if (viewModel.editingReservationId == null && initialDay != -1) {
            viewModel.cancelEditing()
            viewModel.onDateSelected(initialDay)
            viewModel.onTimeChange(initialHour, initialMinute)
        }
        viewModel.checkUserReservationStatus()
        viewModel.loadAndFilterVehicles(context)
        viewModel.validateBooking()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cancelEditing() }
    }

    if (viewModel.showTimePicker) {
        LksTimePicker(
            onConfirm = { h, m ->
                viewModel.onTimeChange(h, m)
                viewModel.onShowTimePickerChange(false)
            },
            onDismiss = { viewModel.onShowTimePickerChange(false) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BookingTopHeader(zone = viewModel.parkingZone)

        Column(modifier = Modifier.padding(20.dp)) {

            // --- VEHÍCULO ---
            VehicleSection(viewModel = viewModel, onManageVehicles = onManageVehicles)

            Spacer(Modifier.height(16.dp))

            // --- FECHAS (SOLO MOSTRAR EN PANTALLA DE EDICION) ---
            if (viewModel.editingReservationId != null) {
                SectionHeader(title = stringResource(R.string.booking_select_date))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    viewModel.availableDates.forEach { (day, label) ->
                        val displayLabel = if (label == "TODAY") todayStr else label
                        DateItem(
                            day = day.toString(),
                            label = displayLabel,
                            isSelected = viewModel.selectedDay == day,
                            onClick = { viewModel.onDateSelected(day) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // --- TIEMPO Y DURACIÓN ---
            TimeAndDurationSection(
                viewModel = viewModel,
                isReadOnly = viewModel.editingReservationId == null
            )

            Spacer(Modifier.height(32.dp))

            // --- CONFIRMACIÓN (PASANDO LA COHERENCIA CORREGIDA) ---
            BookingActionSection(
                viewModel = viewModel,
                isButtonEnabled = isButtonEnabled,
                onConfirmBooking = onConfirmBooking
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- SUB-COMPONENTES ---

@Composable
fun BookingTopHeader(zone: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LksOrange,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DirectionsCar, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.booking_header_label), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(text = zone, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VehicleSection(viewModel: BookingViewModel, onManageVehicles: () -> Unit) {
    SectionHeader(
        title = stringResource(R.string.booking_select_vehicle),
        actionText = stringResource(R.string.booking_manage),
        onActionClick = onManageVehicles
    )

    viewModel.selectedVehicle?.let { vehicle ->
        val vehicleIcon = when (vehicle.type) {
            VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
            VehicleType.ELECTRIC -> Icons.Default.ElectricCar
            VehicleType.ADAPTED -> Icons.AutoMirrored.Filled.Accessible
            else -> Icons.Default.DirectionsCar
        }
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            border = BorderStroke(2.dp, LksOrange),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(vehicleIcon, null, tint = LksOrange)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(vehicle.name, fontWeight = FontWeight.Bold)
                    Text(vehicle.plate, color = Color.Gray)
                }
            }
        }
    } ?: run {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = palePink),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.booking_no_vehicles, viewModel.parkingZone), color = Color.Red, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.booking_add_vehicle_hint), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TimeAndDurationSection(viewModel: BookingViewModel, isReadOnly: Boolean) {
    SectionHeader(title = stringResource(R.string.booking_time_duration))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isReadOnly) "Selected Start Time" else stringResource(R.string.booking_start_time),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            if (isReadOnly) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = bookingCardColor,
                    border = BorderStroke(1.dp, lightGray)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(String.format("%02d:%02d", viewModel.startHour, viewModel.startMinute), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.AccessTime, null, tint = LksOrange)
                    }
                }
            } else {
                OutlinedCard(
                    onClick = { viewModel.onShowTimePickerChange(true) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = bookingCardColor),
                    border = BorderStroke(1.dp, lightGray)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(String.format("%02d:%02d", viewModel.startHour, viewModel.startMinute), style = MaterialTheme.typography.titleLarge)
                        Icon(Icons.Default.AccessTime, null, tint = Color.Gray)
                    }
                }
            }

            DurationBlock(viewModel)
        }
    }
}

@Composable
fun DurationBlock(viewModel: BookingViewModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(stringResource(R.string.booking_duration_label), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.booking_ending_at, viewModel.getEndTime()), color = LksOrange, fontWeight = FontWeight.Bold)
            if (viewModel.isNextDay()) {
                Text(text = stringResource(R.string.booking_next_day_warning), color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(cremaSuave, RoundedCornerShape(12.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = viewModel.duration.toInt().toString(), style = MaterialTheme.typography.displayMedium, color = LksOrange, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.booking_hours_unit), modifier = Modifier.padding(bottom = 12.dp), color = LksOrange, fontWeight = FontWeight.Medium)
            }
            Slider(
                value = viewModel.duration,
                onValueChange = { viewModel.onDurationChange(it) },
                valueRange = 1f..8f,
                colors = SliderDefaults.colors(thumbColor = LksOrange, activeTrackColor = LksOrange, inactiveTrackColor = LksOrange.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
fun BookingActionSection(viewModel: BookingViewModel, isButtonEnabled: Boolean, onConfirmBooking: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

        if (!isButtonEnabled) {
            when {
                viewModel.isOverlapConflict -> {
                    Text(
                        text = "Next booking at ${viewModel.nextCollisionTime} so you can maximum reserve ${viewModel.maxAllowedHours} hours",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                viewModel.isDateTimeValid() && viewModel.selectedVehicle != null -> {
                    Text(
                        text = stringResource(R.string.booking_error_active),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                !viewModel.isDateTimeValid() -> {
                    Text(
                        text = stringResource(R.string.booking_error_past),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        LksButton(
            text = if (viewModel.editingReservationId != null) stringResource(R.string.booking_btn_update) else stringResource(R.string.booking_btn_confirm),
            enabled = isButtonEnabled,
            onClick = {
                val realZone = ParkingMock.zones.find { it.name == viewModel.parkingZone } ?: ParkingMock.zones.first()
                viewModel.selectedVehicle?.let { vehicle ->
                    viewModel.confirmReservation(vehicle, realZone) { onConfirmBooking() }
                }
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
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
                modifier = Modifier.padding(8.dp).clickable { onActionClick() }
            )
        }
    }
}