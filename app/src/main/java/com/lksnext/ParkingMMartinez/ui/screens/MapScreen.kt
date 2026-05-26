package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.DateItem
import com.lksnext.ParkingMMartinez.ui.components.LksHeader
import com.lksnext.ParkingMMartinez.ui.components.LksTimePicker
import com.lksnext.ParkingMMartinez.ui.components.ZoneCard
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.bookingCardColor
import com.lksnext.ParkingMMartinez.ui.theme.lightGray
import com.lksnext.ParkingMMartinez.ui.theme.mistGray
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    bookingViewModel: BookingViewModel,
    onZoneClick: (String) -> Unit
) {
    val todayStr = stringResource(R.string.booking_today)

    LifecycleResumeEffect(Unit) {
        bookingViewModel.cancelEditing()
        bookingViewModel.checkUserReservationStatus()
        viewModel.refreshParkingStatus()

        onPauseOrDispose {
            // Por si hiciera falta limpiar recursos
        }
    }

    // Diálogo emergente nativo para seleccionar la hora
    if (viewModel.showTimePicker) {
        LksTimePicker(
            onConfirm = { h, m ->
                viewModel.onTimeChange(h, m)
                viewModel.showTimePicker = false
            },
            onDismiss = { viewModel.showTimePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(mistGray)
    ) {
        LksHeader(
            title = stringResource(R.string.map_header_title),
            subtitle = stringResource(R.string.map_header_subtitle)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // --- SELECTOR DE FECHAS HORIZONTAL ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                viewModel.availableDates.forEach { (day, label) ->
                    val displayLabel = if (label == "TODAY") todayStr else label
                    DateItem(
                        day = day.toString(),
                        label = displayLabel,
                        isSelected = viewModel.selectedDayNumber == day,
                        onClick = { viewModel.onDateSelected(day) }
                    )
                }
            }

            // --- SELECTOR DE HORA DE INICIO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.booking_start_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    OutlinedCard(
                        onClick = { viewModel.showTimePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = bookingCardColor),
                        border = BorderStroke(1.dp, lightGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format("%02d:%02d", viewModel.selectedStartTime.hour, viewModel.selectedStartTime.minute),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(Icons.Default.AccessTime, null, tint = Color.Gray)
                        }
                    }
                }
            }

            // --- ZONAS DE APARCAMIENTO ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                viewModel.zones.forEach { zone ->
                    ZoneCard(
                        zone = zone,
                        onClick = { onZoneClick(zone.name) }
                    )
                }
            }
        }
    }
}