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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.ui.components.LksHeader
import com.lksnext.ParkingMMartinez.ui.components.ZoneCard
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
import com.lksnext.ParkingMMartinez.model.*
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.theme.mistGray

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onZoneClick: (String) -> Unit
) {

    // De momento vamos a hacer el import del mock para que tenga los datos

    LaunchedEffect(Unit) {
        viewModel.refreshParkingStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(mistGray)
    ) {
        LksHeader(
            title = stringResource(R.string.map_header_title),//"LKS Next Parking",
            subtitle = stringResource(R.string.map_header_subtitle)//"Select a zone to book instantly"
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            viewModel.zones.forEach { zone ->
                ZoneCard(
                    zone = zone,//updatedZone,
                    onClick = { onZoneClick(zone.name) }
                )
            }
        }
    }
}