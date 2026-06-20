package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.theme.activeYelow
import com.lksnext.ParkingMMartinez.ui.theme.LksGreen
import com.lksnext.ParkingMMartinez.ui.theme.automaticRed
import com.lksnext.ParkingMMartinez.ui.theme.cremaSuave
import com.lksnext.ParkingMMartinez.ui.theme.mistGray

data class ReservationActions(
    val onCancelClick: () -> Unit,
    val onCheckInClick: () -> Unit,
    val onEditClick: () -> Unit
)

data class ReservationTestTags(
    val cardModifier: Modifier = Modifier,
    val cancelBtnModifier: Modifier = Modifier,
    val editBtnModifier: Modifier = Modifier,
    val checkInBtnModifier: Modifier = Modifier
)

@Composable
fun ReservationCard(
    reservation: Reservation,
    isPast: Boolean,
    isCheckInWindowActive: Boolean = false,
    actions: ReservationActions,
    tags: ReservationTestTags = ReservationTestTags()
) {
    val isMissed = isPast && !reservation.isCheckedIn
    val cardBorder = if (isMissed) BorderStroke(2.dp, Color.Red) else null

    Card(
        modifier = tags.cardModifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ReservationHeader(reservation = reservation, isPast = isPast, isMissed = isMissed)
            ReservationDetails(reservation = reservation)

            if (!isPast) {
                ReservationActionButtons(
                    reservation = reservation,
                    isCheckInWindowActive = isCheckInWindowActive,
                    actions = actions,
                    tags = tags
                )
            }
        }
    }
}

@Composable
private fun ReservationHeader(reservation: Reservation, isPast: Boolean, isMissed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = when {
                isMissed -> Color.Red
                isPast -> Color.Gray
                else -> LksOrange
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(45.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(reservation.spotNumber.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(reservation.zone.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(" ${stringResource(R.string.label_location)}", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Surface(
            color = when {
                isMissed -> automaticRed
                isPast -> mistGray
                else -> cremaSuave
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = when {
                    isMissed -> stringResource(R.string.reservation_missed)
                    isPast -> stringResource(R.string.reservation_past)
                    reservation.isCheckedIn -> stringResource(R.string.reservation_checked_in)
                    else -> stringResource(R.string.status_active)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = when {
                    isMissed -> Color.Red
                    isPast -> Color.DarkGray
                    reservation.isCheckedIn -> LksGreen
                    else -> activeYelow
                },
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ReservationDetails(reservation: Reservation) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        color = mistGray,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DetailItem(
                    label = stringResource(R.string.label_date),
                    value = java.text.SimpleDateFormat("MMM d, yyyy").format(reservation.date),
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = stringResource(R.string.label_time),
                    value = "${reservation.startTime} - ${reservation.endTime}",
                    modifier = Modifier.weight(1f),
                    isTime = true
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_vehicle),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Text(" ${reservation.vehicle.name} (${reservation.vehicle.plate})", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ReservationActionButtons(
    reservation: Reservation,
    isCheckInWindowActive: Boolean,
    actions: ReservationActions,
    tags: ReservationTestTags
) {
    val canDoCheckIn = isCheckInWindowActive && !reservation.isCheckedIn

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Cancelar
        OutlinedButton(
            onClick = actions.onCancelClick,
            modifier = tags.cancelBtnModifier
                .weight(0.9f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_cancel),
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        // Botón Editar
        OutlinedButton(
            onClick = actions.onEditClick,
            modifier = tags.editBtnModifier
                .weight(0.9f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_edit),
                color = LksOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        // Botón Check-in
        Button(
            onClick = actions.onCheckInClick,
            enabled = canDoCheckIn,
            modifier = tags.checkInBtnModifier
                .weight(1.4f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (reservation.isCheckedIn) Color.Gray else LksGreen,
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                text = if (reservation.isCheckedIn) stringResource(R.string.reservation_done) else stringResource(R.string.btn_check_in),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = if (canDoCheckIn || reservation.isCheckedIn) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier, isTime: Boolean = false) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isTime) Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = LksOrange)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = if(isTime) 4.dp else 0.dp))
        }
    }
}