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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancelClick: () -> Unit,
    onCheckInClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        Text(" Level 1", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "ACTIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFFFBC02D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem("DATE", java.text.SimpleDateFormat("MMM d, yyyy").format(reservation.date), Modifier.weight(1f))
                        DetailItem("TIME", "${reservation.startTime} - ${reservation.endTime}", Modifier.weight(1f), isTime = true)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("VEHICLE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Text(" ${reservation.vehicle.name} (${reservation.vehicle.plate})", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("Cancel", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onCheckInClick,
                    modifier = Modifier.weight(1.2f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A650))
                ) {
                    Text("Check-in Now", fontWeight = FontWeight.Bold)
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