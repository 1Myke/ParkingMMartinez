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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.DateItem
import com.lksnext.ParkingMMartinez.ui.components.LksHeader
import com.lksnext.ParkingMMartinez.ui.components.LksTimePicker
import com.lksnext.ParkingMMartinez.ui.components.ZoneCard
import com.lksnext.ParkingMMartinez.ui.theme.bookingCardColor
import com.lksnext.ParkingMMartinez.ui.theme.lightGray
import com.lksnext.ParkingMMartinez.ui.theme.mistGray
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    bookingViewModel: BookingViewModel,
    onZoneClick: (String) -> Unit
) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    LifecycleResumeEffect(Unit) {
        bookingViewModel.cancelEditing()
        bookingViewModel.checkUserReservationStatus()

        //Metodo manual, ya que no disponemos de los servicios cloud de firebase
        val job = scope.launch {
            while (true) {
                viewModel.refreshParking()
                delay(30_000L)
            }
        }

        onPauseOrDispose {
            job.cancel()
        }
    }

    if (viewModel.showTimePicker) {
        LksTimePicker(
            onConfirm = { h, m ->
                viewModel.onTimeChange(h, m)
                viewModel.showTimePicker = false
            },
            onDismiss = { viewModel.showTimePicker = false },
            modifier = Modifier.testTag(TestTags.TIME_PICKER_DIALOG),
            confirmButtonModifier = Modifier.testTag(TestTags.TIME_PICKER_CONFIRM),
            dismissButtonModifier = Modifier.testTag(TestTags.TIME_PICKER_CANCEL)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LksHeader(
            title = stringResource(R.string.map_header_title),
            subtitle = stringResource(R.string.map_header_subtitle),
            modifier = Modifier.testTag(TestTags.MAP_HEADER)
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
            DateSelectorSection(viewModel)

            // --- SELECTOR DE HORA DE INICIO ---
            TimeSelectorSection(viewModel)

            // --- ZONAS DE APARCAMIENTO ---
            ParkingZonesSection(viewModel, bookingViewModel, context, onZoneClick)
        }
    }
}

@Composable
private fun DateSelectorSection(viewModel: MapViewModel) {
    val todayStr = stringResource(R.string.booking_today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        viewModel.availableDates.forEach { (fullDate, label) ->
            val displayLabel = if (label == "TODAY") todayStr else {
                val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())

                sdf.format(fullDate).replaceFirstChar { it.uppercase() }
            }

            val cal = Calendar.getInstance().apply { time = fullDate }
            val dayNumStr = cal.get(Calendar.DAY_OF_MONTH).toString()

            DateItem(
                day = dayNumStr,
                label = displayLabel,
                isSelected = viewModel.selectedDate == fullDate,
                modifier = Modifier.testTag("${TestTags.MAP_DATE_ITEM_PREFIX}$dayNumStr"),
                onClick = { viewModel.onDateSelected(fullDate) }
            )
        }
    }
}

@Composable
private fun TimeSelectorSection(viewModel: MapViewModel) {
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
                    .padding(top = 8.dp)
                    .testTag(TestTags.MAP_TIME_PICKER_TRIGGER),
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
                        text = java.lang.String.format("%02d:%02d", viewModel.selectedStartTime.hour, viewModel.selectedStartTime.minute),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(Icons.Default.AccessTime, null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun ParkingZonesSection(
    viewModel: MapViewModel,
    bookingViewModel: BookingViewModel,
    context: android.content.Context,
    onZoneClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        viewModel.zones.forEach { zone ->
            val isZoneFull = zone.availableSpots <= 0

            ZoneCard(
                zone         = zone,
                modifier     = Modifier.testTag("${TestTags.MAP_ZONE_CARD_PREFIX}${zone.name}"),
                isSubscribed = zone.name in viewModel.subscribedZoneNames,
                onBellClick  = { viewModel.toggleZoneSubscription(context, zone.name) },
                onClick = {
                    if (!isZoneFull) {
                        bookingViewModel.onDateSelected(viewModel.selectedDate)
                        onZoneClick(zone.name)
                    }
                }
            )
        }
    }
}