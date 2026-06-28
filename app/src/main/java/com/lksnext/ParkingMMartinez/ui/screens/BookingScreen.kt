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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTimePicker
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.data.ParkingManager
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.ui.components.DateItem
import com.lksnext.ParkingMMartinez.ui.components.TimeSlider
import com.lksnext.ParkingMMartinez.ui.theme.lightGray
import com.lksnext.ParkingMMartinez.ui.theme.bookingCardColor
import com.lksnext.ParkingMMartinez.ui.theme.cremaSuave
import com.lksnext.ParkingMMartinez.ui.theme.palePink
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import java.util.Calendar
import java.util.Date

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    initialZone: String = "",
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onConfirmBooking: () -> Unit = {},
    onManageVehicles: () -> Unit = {}
) {
    val isButtonEnabled = viewModel.isButtonEnabled
    val isCheckedIn = viewModel.isEditingCheckedIn

    BookingInitializationEffect(viewModel, initialZone, initialHour, initialMinute)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelEditing()
            viewModel.resetLoadingState()
        }
    }

    if (viewModel.showTimePicker) {
        BookingTimePickerWrapper(viewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BookingTopHeader(zone = viewModel.parkingZone)

        Column(modifier = Modifier.padding(20.dp)) {
            VehicleSection(
                viewModel = viewModel,
                onManageVehicles = onManageVehicles,
                isEnabled = !isCheckedIn
            )

            Spacer(Modifier.height(16.dp))

            DateSelectionSection(viewModel = viewModel, isCheckedIn = isCheckedIn)

            TimeAndDurationSection(
                viewModel = viewModel,
                isReadOnly = viewModel.editingReservationId == null || isCheckedIn
            )

            Spacer(Modifier.height(32.dp))

            BookingActionSection(
                viewModel = viewModel,
                isButtonEnabled = isButtonEnabled,
                onConfirmBooking = onConfirmBooking
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BookingInitializationEffect(
    viewModel: BookingViewModel,
    initialZone: String,
    initialHour: Int,
    initialMinute: Int
) {
    LaunchedEffect(initialZone, initialHour, initialMinute) {
        viewModel.setZone(initialZone)

        if (viewModel.editingReservationId == null) {
            viewModel.cancelEditing()
            viewModel.onTimeChange(initialHour, initialMinute)
        }
        viewModel.checkUserReservationStatus()
        viewModel.loadAndFilterVehicles()
        viewModel.validateBooking()
    }
}

@Composable
private fun BookingTimePickerWrapper(viewModel: BookingViewModel) {
    LksTimePicker(
        onConfirm = { h, m ->
            viewModel.onTimeChange(h, m)
            viewModel.onShowTimePickerChange(false)
        },
        onDismiss = { viewModel.onShowTimePickerChange(false) },
        modifier = Modifier.testTag(TestTags.TIME_PICKER_DIALOG),
        confirmButtonModifier = Modifier.testTag(TestTags.TIME_PICKER_CONFIRM),
        dismissButtonModifier = Modifier.testTag(TestTags.TIME_PICKER_CANCEL)
    )
}

@Composable
fun DateSelectionSection(viewModel: BookingViewModel, isCheckedIn: Boolean) {
    if (viewModel.editingReservationId == null) return

    val todayStr = stringResource(R.string.booking_today)

    SectionHeader(title = stringResource(R.string.booking_select_date))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        viewModel.availableDates.forEach { (dayInt, label) ->
            val displayLabel = if (label == "TODAY") todayStr else label
            val isSelected = isDateMatchingDay(viewModel.selectedDate, dayInt)

            DateItem(
                day = dayInt.toString(),
                label = displayLabel,
                isSelected = isSelected,
                modifier = Modifier.testTag("${TestTags.BOOKING_DATE_ITEM_PREFIX}$dayInt"),
                onClick = {
                    if (!isCheckedIn) {
                        viewModel.onDateSelected(calculateTargetDate(dayInt))
                    }
                }
            )
        }
    }
    Spacer(Modifier.height(24.dp))
}

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
                Text(
                    text = stringResource(id = getZoneDisplayNameRes(zone)),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(TestTags.BOOKING_SCREEN_HEADER_ZONE)
                )
            }
        }
    }
}

@Composable
fun VehicleSection(
    viewModel: BookingViewModel,
    onManageVehicles: () -> Unit,
    isEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    SectionHeader(
        title = stringResource(R.string.booking_select_vehicle),
        actionText = if (isEnabled) stringResource(R.string.booking_manage) else null,
        onActionClick = onManageVehicles,
        actionModifier = Modifier.testTag(TestTags.BOOKING_MANAGE_VEHICLES_BTN)
    )

    val currentVehicle = viewModel.selectedVehicle
    val vehiclesList = viewModel.userVehicles

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            // Solo un vehiculo, se selecciona autoamaticamente
            currentVehicle != null -> {
                SelectedVehicleCard(
                    vehicle = currentVehicle,
                    hasMultipleOptions = isEnabled && vehiclesList.size > 1,
                    isEnabled = isEnabled,
                    onClick = { expanded = true }
                )
            }

            // Hay varios vehiculos del mismo tipo, obligatorio seleccionar cual
            currentVehicle == null && vehiclesList.isNotEmpty() -> {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = isEnabled) { expanded = true }
                        .testTag("BOOKING_SELECT_REQUIRED_CARD"),
                    border = BorderStroke(2.dp, Color.Red),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFFF2F2))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(Modifier.width(16.dp))

                        Text(
                            text = stringResource(R.string.booking_select_vehicle_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Lista de vehiculos vacia, error
            else -> {
                NoVehiclesErrorCard(parkingZone = viewModel.parkingZone)
            }
        }

        VehicleDropdownMenu(
            expanded = expanded,
            vehicles = vehiclesList,
            onDismiss = { expanded = false },
            onVehicleSelected = { vehicle ->
                viewModel.onVehicleSelected(vehicle)
                expanded = false
            }
        )
    }
}

@Composable
private fun SelectedVehicleCard(
    vehicle: Vehicle,
    hasMultipleOptions: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = hasMultipleOptions, onClick = onClick)
            .testTag(TestTags.BOOKING_SELECTED_VEHICLE_CARD),
        border = BorderStroke(2.dp, if (isEnabled) LksOrange else Color.LightGray),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getVehicleIcon(vehicle.type),
                contentDescription = null,
                tint = if (isEnabled) LksOrange else Color.Gray
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.name,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.Unspecified else Color.Gray
                )
                Text(text = vehicle.plate, color = Color.Gray)
            }
            if (hasMultipleOptions) {
                Text(
                    text = stringResource(R.string.booking_change_vehicle_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = LksOrange
                )
            }
        }
    }
}

@Composable
private fun VehicleDropdownMenu(
    expanded: Boolean,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onVehicleSelected: (Vehicle) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.White)
    ) {
        vehicles.forEach { option ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getVehicleIcon(option.type),
                            contentDescription = null,
                            tint = LksOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = option.name, fontWeight = FontWeight.SemiBold)
                            Text(text = option.plate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                },
                onClick = { onVehicleSelected(option) }
            )
        }
    }
}

@Composable
private fun NoVehiclesErrorCard(parkingZone: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag(TestTags.BOOKING_NO_VEHICLES_ERROR),
        colors = CardDefaults.cardColors(containerColor = palePink),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val translatedZone = stringResource(id = getZoneDisplayNameRes(parkingZone))

            Text(
                text = stringResource(R.string.booking_no_vehicles, translatedZone),
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.booking_add_vehicle_hint),
                style = MaterialTheme.typography.bodySmall
            )
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
                text = if (isReadOnly) stringResource(R.string.booking_selected_start_time) else stringResource(R.string.booking_start_time),
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
                        Text(String.format("%02d:%02d", viewModel.startHour, viewModel.startMinute), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Icon(Icons.Default.AccessTime, null, tint = Color.Gray)
                    }
                }
            } else {
                OutlinedCard(
                    onClick = { viewModel.onShowTimePickerChange(true) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(TestTags.BOOKING_TIME_PICKER_TRIGGER),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = bookingCardColor),
                    border = BorderStroke(1.dp, lightGray)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(String.format("%02d:%02d", viewModel.startHour, viewModel.startMinute), style = MaterialTheme.typography.titleLarge)
                        Icon(Icons.Default.AccessTime, null, tint = LksOrange)
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
                Text(" " + stringResource(R.string.booking_hours_unit), modifier = Modifier.padding(bottom = 12.dp), color = LksOrange, fontWeight = FontWeight.Medium)
            }
            TimeSlider(
                currentHours = viewModel.duration,
                onHoursChange = { viewModel.onDurationChange(it) },
                modifier = Modifier.testTag(TestTags.BOOKING_DURATION_SLIDER)
            )
        }
    }
}

@Composable
fun BookingActionSection(viewModel: BookingViewModel, isButtonEnabled: Boolean, onConfirmBooking: () -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isButtonEnabled) {
            BookingErrorMessages(viewModel)
        }

        LksButton(
            text = if (viewModel.editingReservationId != null) stringResource(R.string.booking_btn_update) else stringResource(R.string.booking_btn_confirm),
            enabled = isButtonEnabled && !viewModel.isLoading,
            modifier = Modifier.testTag(TestTags.BOOKING_SUBMIT_BTN),
            onClick = {
                val realZone = ParkingManager.zones.find { it.name == viewModel.parkingZone } ?: ParkingManager.zones.first()
                viewModel.selectedVehicle?.let { vehicle ->
                    viewModel.confirmReservation(context, vehicle, realZone) {
                        onConfirmBooking()
                    }
                }
            }
        )
    }
}

@Composable
private fun BookingErrorMessages(viewModel: BookingViewModel) {
    when {
        viewModel.selectedVehicle == null && viewModel.userVehicles.isNotEmpty() -> {
            Text(
                text = stringResource(R.string.booking_error_vehicle_required),
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("BOOKING_ERROR_VEHICLE_REQUIRED"),
                textAlign = TextAlign.Center
            )
        }
        viewModel.isOverlapConflict -> {
            Text(
                text = stringResource(R.string.booking_error_overlap, viewModel.nextCollisionTime ?: "", viewModel.maxAllowedHours),
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp).testTag(TestTags.BOOKING_ERROR_OVERLAP),
                textAlign = TextAlign.Center
            )
        }
        viewModel.isDateTimeValid() && viewModel.selectedVehicle != null -> {
            Text(
                text = stringResource(R.string.booking_error_active),
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp).testTag(TestTags.BOOKING_ERROR_ACTIVE),
                textAlign = TextAlign.Center
            )
        }
        !viewModel.isDateTimeValid() -> {
            Text(
                text = stringResource(R.string.booking_error_past),
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp).testTag(TestTags.BOOKING_ERROR_PAST),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    actionModifier: Modifier = Modifier,
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
                modifier = actionModifier.padding(8.dp).clickable { onActionClick() }
            )
        }
    }
}

private fun isDateMatchingDay(selectedDate: Date, dayInt: Int): Boolean {
    val calSelected = Calendar.getInstance().apply { time = selectedDate }
    return calSelected.get(Calendar.DAY_OF_MONTH) == dayInt
}

private fun calculateTargetDate(dayInt: Int): Date {
    val targetCal = Calendar.getInstance()
    val currentDay = targetCal.get(Calendar.DAY_OF_MONTH)

    if (dayInt < currentDay) {
        targetCal.add(Calendar.MONTH, 1)
    }

    targetCal.set(Calendar.DAY_OF_MONTH, dayInt)
    targetCal.set(Calendar.HOUR_OF_DAY, 0)
    targetCal.set(Calendar.MINUTE, 0)
    targetCal.set(Calendar.SECOND, 0)
    targetCal.set(Calendar.MILLISECOND, 0)

    return targetCal.time
}

private fun getVehicleIcon(type: VehicleType) = when (type) {
    VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
    VehicleType.ELECTRIC -> Icons.Default.ElectricCar
    VehicleType.ADAPTED -> Icons.AutoMirrored.Filled.Accessible
    else -> Icons.Default.DirectionsCar
}

fun getZoneDisplayNameRes(zoneName: String): Int {
    return when (zoneName) {
        com.lksnext.ParkingMMartinez.model.ZoneNames.DISABILITY -> R.string.zone_disability
        com.lksnext.ParkingMMartinez.model.ZoneNames.EV -> R.string.zone_ev
        com.lksnext.ParkingMMartinez.model.ZoneNames.MOTORCYCLE -> R.string.zone_motorcycle
        else -> R.string.zone_standard
    }
}