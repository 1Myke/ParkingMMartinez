package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.components.ReservationCard
import com.lksnext.ParkingMMartinez.ui.components.ReservationActions
import com.lksnext.ParkingMMartinez.ui.components.ReservationTestTags
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun BookingRegisterScreen(
    viewModel: BookingRegisterViewModel,
    bookingViewModel: BookingViewModel,
    onNavigateToEdit: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }
    val context = LocalContext.current

    val currentTab = viewModel.selectedTab
    val currentReservations = if (currentTab == 0) viewModel.activeReservations else viewModel.pastReservations
    val isPastTab = currentTab == 1

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(TestTags.BOOKING_REGISTER_TITLE)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de Pestañas
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = Color.Transparent,
            contentColor = LksOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                    color = LksOrange
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("BOOKING_TAB_ROW")
        ) {
            Tab(
                selected = currentTab == 0,
                onClick = { viewModel.selectedTab = 0 },
                text = {
                    Text(
                        text = stringResource(R.string.notification_tab_active, viewModel.activeReservations.size),
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = LksOrange,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("BOOKING_TAB_ACTIVE")
            )
            Tab(
                selected = currentTab == 1,
                onClick = { viewModel.selectedTab = 1 },
                text = {
                    Text(
                        text = stringResource(R.string.notification_tab_past, viewModel.pastReservations.size),
                        fontWeight = FontWeight.Bold
                    )
                },
                selectedContentColor = LksOrange,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("BOOKING_TAB_PAST")
            )
        }

        // Listas de Reservas
        if (currentReservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().testTag(TestTags.BOOKING_REGISTER_EMPTY),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPastTab) stringResource(R.string.notification_past_empty) else stringResource(R.string.register_empty),
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(currentReservations) { reservation ->

                    // Puede que esto haya que borrarlo MIKEL
                    val isCheckInEnabled = viewModel.isCheckInWindowActive(reservation) && !reservation.isCheckedIn

                    ReservationCard(
                        reservation = reservation,
                        isPast = isPastTab,
                        isCheckInWindowActive = viewModel.isCheckInWindowActive(reservation),
                        actions = ReservationActions(
                            onCancelClick = {
                                // Cancel in the register VM first (removes from Firestore + alarms)
                                viewModel.cancelReservation(context, reservation.id)
                                // Then let the shared booking VM reset the 50 % guard if the
                                // zone+day drops back below the threshold after this cancellation.
                                bookingViewModel.onReservationCancelled(context, reservation)
                            },
                            onCheckInClick = {
                                if (isCheckInEnabled) {
                                    viewModel.doCheckIn(reservation)
                                }
                            },
                            onEditClick = {
                                bookingViewModel.loadReservationForEditing(reservation)
                                onNavigateToEdit(reservation.zone.name)
                            }
                        ),
                        tags = ReservationTestTags(
                            cardModifier = Modifier.testTag("${TestTags.RESERVATION_CARD_PREFIX}${reservation.id}"),
                            cancelBtnModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_CANCEL_PREFIX}${reservation.id}"),
                            editBtnModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_EDIT_PREFIX}${reservation.id}"),
                            checkInBtnModifier = Modifier.testTag("${TestTags.RESERVATION_BTN_CHECKIN_PREFIX}${reservation.id}")
                        )
                    )
                }
            }
        }
    }
}