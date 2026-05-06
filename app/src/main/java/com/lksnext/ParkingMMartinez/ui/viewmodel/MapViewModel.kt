package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.model.ParkingZone

class MapViewModel : ViewModel() {
    val zones: List<ParkingZone> = ParkingMock.zones
}