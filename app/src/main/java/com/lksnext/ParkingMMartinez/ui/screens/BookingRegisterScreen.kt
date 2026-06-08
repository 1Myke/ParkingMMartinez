package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.components.ReservationCard
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.constants.TestTags

@Composable
fun BookingRegisterScreen(
    viewModel: BookingRegisterViewModel,
    bookingViewModel: BookingViewModel,
    onNavigateToEdit: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(TestTags.BOOKING_REGISTER_TITLE)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.reservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().testTag(TestTags.BOOKING_REGISTER_EMPTY),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.register_empty), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancelClick = { viewModel.cancelReservation(reservation.id) },
                        onCheckInClick = { viewModel.doCheckIn(reservation.id) },
                        onEditClick = {
                            bookingViewModel.loadReservationForEditing(reservation)
                            onNavigateToEdit(reservation.zone.name)
                        },

                        modifier = Modifier.testTag("${TestTags.RESERVATION_CARD_PREFIX}${reservation.id}"),
                        cancelButtonModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_CANCEL_PREFIX}${reservation.id}"),
                        editButtonModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_EDIT_PREFIX}${reservation.id}"),
                        checkInButtonModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_CHECKIN_PREFIX}${reservation.id}")
                    )
                }
            }
        }
    }
}