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
import com.lksnext.ParkingMMartinez.ui.theme.cremaSuave
import com.lksnext.ParkingMMartinez.ui.theme.mistGray

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    cancelButtonModifier: Modifier = Modifier,
    editButtonModifier: Modifier = Modifier,
    checkInButtonModifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LksOrange,
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
                        // Aquí usamos el string del nivel
                        Text(" ${stringResource(R.string.label_location)}", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Surface(
                    color = cremaSuave,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.status_active), // "ACTIVE"
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = activeYelow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                color = mistGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            label = stringResource(R.string.label_date), // "DATE"
                            value = java.text.SimpleDateFormat("MMM d, yyyy").format(reservation.date),
                            modifier = Modifier.weight(1f)
                        )
                        DetailItem(
                            label = stringResource(R.string.label_time), // "TIME"
                            value = "${reservation.startTime} - ${reservation.endTime}",
                            modifier = Modifier.weight(1f),
                            isTime = true
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.label_vehicle), // "VEHICLE"
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Text(" ${reservation.vehicle.name} (${reservation.vehicle.plate})", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), // Un poco de aire extra arriba
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Reducimos el espacio entre ellos a 8.dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Cancelar
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = cancelButtonModifier
                        .weight(0.9f) // Un poco más pequeño
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    contentPadding = PaddingValues(horizontal = 4.dp) // Evita que el texto interno empuje los bordes
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, // Ajustamos un pelín el tamaño
                        maxLines = 1
                    )
                }

                // Botón Editar
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = editButtonModifier
                        .weight(0.9f) // Igual que el de cancelar
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

                // Botón Check-in (El protagonista)
                Button(
                    onClick = onCheckInClick,
                    modifier = checkInButtonModifier
                        .weight(1.4f) // Le damos más peso para que el texto quepa bien
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LksGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_check_in),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier, isTime: Boolean = false) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isTime) Icon(androidx.compose.material.icons.Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = LksOrange)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = if(isTime) 4.dp else 0.dp))
        }
    }
}