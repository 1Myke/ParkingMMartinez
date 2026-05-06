package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.ui.components.LksHeader
import com.lksnext.ParkingMMartinez.ui.components.ZoneCard
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import com.lksnext.ParkingMMartinez.model.*

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onZoneClick: (String) -> Unit
) {

    // De momento vamos a hacer el import del mock para que tenga los datos
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshParkingStatus(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        LksHeader(
            title = "LKS Next Parking",
            subtitle = "Select a zone to book instantly"
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            viewModel.zones.forEach { zone ->
                val type: VehicleType = when (zone.name) {
                    ZoneNames.DISABILITY -> VehicleType.ADAPTED
                    ZoneNames.EV -> VehicleType.ELECTRIC
                    ZoneNames.MOTORCYCLE -> VehicleType.MOTORCYCLE
                    ZoneNames.STANDARD -> VehicleType.STANDARD
                    else -> {
                        android.util.Log.e("MAP_ERROR", "ZoneName no reconocido: ${zone.name}")
                        VehicleType.STANDARD
                    }
                }

                val available = ParkingMock.getAvailableSpotsCount(type)
                val total = ParkingMock.getTotalSpotsCount(type)

                // Creamos una copia de la zona con los datos actualizados para la Card
                val updatedZone = zone.copy(
                    availableSpots = available,
                    totalSpots = total
                )

                ZoneCard(
                    zone = updatedZone,
                    onClick = { onZoneClick(zone.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapScreenPreview() {
    MapScreen(
        onZoneClick = {zoneName -> }
    )
}