package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository

class MapViewModel(
    private val repository: BookingRepository
) : ViewModel() {
//    val zones: List<ParkingZone> = ParkingMock.zones

    var zones by mutableStateOf(ParkingMock.zones)
        private set

    fun refreshParkingStatus() {
        val allBookings = repository.getAllReservations()

        android.util.Log.d("DEBUG_MAP", "Reservas leídas del disco: ${allBookings.size}")

        ParkingMock.syncWithReservations(allBookings)

        zones = ParkingMock.zones.toList()
    }
}