package com.lksnext.ParkingMMartinez.ui.screens

import android.widget.Space
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.ui.components.ReservationCard
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.R

@Composable
fun BookingRegisterScreen(
    viewModel: BookingRegisterViewModel,
    bookingViewModel: BookingViewModel,
    onNavigateToEdit: (String) -> Unit
) {
    // Recargar datos cada vez que entramos a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.reservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.register_empty), color = Color.Gray)
            } // "No active reservations"            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancelClick = {
                            viewModel.cancelReservation(reservation.id)
                        },
                        onCheckInClick = {
                            viewModel.doCheckIn(reservation.id)
                        },
                        onEditClick = {
                            bookingViewModel.loadReservationForEditing(reservation)
                            onNavigateToEdit(reservation.zone.name)
                        }
                    )
                }
            }
        }
    }
}

/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BookingRegisterScreenPreview() {
    BookingRegisterScreen(
        onNavigateToEdit = { }
    )
}
 */