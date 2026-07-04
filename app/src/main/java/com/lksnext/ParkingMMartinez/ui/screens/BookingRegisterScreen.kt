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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.lksnext.ParkingMMartinez.model.Reservation

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

        // Componente de pestañas de develop
        BookingTabs(
            currentTab = currentTab,
            activeCount = viewModel.activeReservations.size,
            pastCount = viewModel.pastReservations.size,
            onTabSelected = { viewModel.selectedTab = it }
        )

        // Control de estado vacío o lista de develop
        if (currentReservations.isEmpty()) {
            EmptyState(isPastTab = isPastTab)
        } else {
            ReservationList(
                reservations = currentReservations,
                isPastTab = isPastTab,
                viewModel = viewModel,
                bookingViewModel = bookingViewModel,
                onNavigateToEdit = onNavigateToEdit
            )
        }

        // Diálogo de confirmación con tu bookingViewModel inyectado
        if (viewModel.showCancelConfirmation) {
            CancelConfirmationDialog(
                viewModel = viewModel,
                bookingViewModel = bookingViewModel,
                context = context
            )
        }
    }
}

@Composable
private fun BookingTabs(
    currentTab: Int,
    activeCount: Int,
    pastCount: Int,
    onTabSelected: (Int) -> Unit
) {
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
            onClick = { onTabSelected(0) },
            text = {
                Text(
                    text = stringResource(R.string.notification_tab_active, activeCount),
                    fontWeight = FontWeight.Bold
                )
            },
            selectedContentColor = LksOrange,
            unselectedContentColor = Color.Gray,
            modifier = Modifier.testTag("BOOKING_TAB_ACTIVE")
        )
        Tab(
            selected = currentTab == 1,
            onClick = { onTabSelected(1) },
            text = {
                Text(
                    text = stringResource(R.string.notification_tab_past, pastCount),
                    fontWeight = FontWeight.Bold
                )
            },
            selectedContentColor = LksOrange,
            unselectedContentColor = Color.Gray,
            modifier = Modifier.testTag("BOOKING_TAB_PAST")
        )
    }
}

@Composable
private fun EmptyState(isPastTab: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().testTag(TestTags.BOOKING_REGISTER_EMPTY),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPastTab) stringResource(R.string.notification_past_empty) else stringResource(R.string.register_empty),
            color = Color.Gray
        )
    }
}

@Composable
private fun ReservationList(
    reservations: List<Reservation>,
    isPastTab: Boolean,
    viewModel: BookingRegisterViewModel,
    bookingViewModel: BookingViewModel,
    onNavigateToEdit: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(reservations) { reservation ->
            val isCheckInEnabled = viewModel.isCheckInWindowActive(reservation) && !reservation.isCheckedIn

            ReservationCard(
                reservation = reservation,
                isPast = isPastTab,
                isCheckInWindowActive = viewModel.isCheckInWindowActive(reservation),
                actions = ReservationActions(
                    // Llama al flujo de confirmación de develop
                    onCancelClick = { viewModel.askCancelReservation(reservation.id) },
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

@Composable
fun CancelConfirmationDialog(
    viewModel: BookingRegisterViewModel,
    bookingViewModel: BookingViewModel,
    context: android.content.Context
) {
    val reservationId = viewModel.reservationToCancel
    val reservation = viewModel.activeReservations.find { it.id == reservationId }
        ?: viewModel.pastReservations.find { it.id == reservationId }

    AlertDialog(
        onDismissRequest = { viewModel.dismissCancelDialog() },
        title = { Text(text = stringResource(R.string.reservation_cancel_title)) },
        text = { Text(text = stringResource(R.string.reservation_cancel_msg)) },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.confirmCancelReservation(context) {
                        if (reservation != null) {
                            bookingViewModel.onReservationCancelled(context, reservation)
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.reservation_cancel_confirm),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.dismissCancelDialog() }
            ) {
                Text(text = stringResource(R.string.btn_cancel), color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}