package com.lksnext.ParkingMMartinez.data

import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.model.ParkingSpot
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames


object  ParkingMock {

    val zones: List<ParkingZone>
        get() = listOf(
            ParkingZone(ZoneNames.DISABILITY, getTotalSpotsCount(VehicleType.ADAPTED), getAvailableSpotsCount(VehicleType.ADAPTED), 0, Color(0xFF2D5AF0)),
            ParkingZone(ZoneNames.EV, getTotalSpotsCount(VehicleType.ELECTRIC), getAvailableSpotsCount(VehicleType.ELECTRIC), 0, Color(0xFF00C853)),
            ParkingZone(ZoneNames.STANDARD, getTotalSpotsCount(VehicleType.STANDARD), getAvailableSpotsCount(VehicleType.STANDARD), 0, Color(0xFF455A64)),
            ParkingZone(ZoneNames.MOTORCYCLE, getTotalSpotsCount(VehicleType.MOTORCYCLE), getAvailableSpotsCount(VehicleType.MOTORCYCLE), 0, Color(0xFFA66FB5))
        )

    private val spots = mutableListOf<ParkingSpot>().apply {
        repeat(6) { add(ParkingSpot(it + 1, VehicleType.ADAPTED))}
        repeat(4) { add(ParkingSpot(it + 7, VehicleType.ELECTRIC))}
        repeat(24) { add(ParkingSpot(it + 11, VehicleType.STANDARD))}
        repeat(16) { add(ParkingSpot(it + 35, VehicleType.MOTORCYCLE))}
    }

    // Cuenta cuántas plazas hay libres para un tipo concreto
    fun getAvailableSpotsCount(type: VehicleType): Int {
        return spots.count { it.zone == type && !it.isOccupied }
    }

    // Cuenta el total de plazas de un tipo
    fun getTotalSpotsCount(type: VehicleType): Int {
        return spots.count { it.zone == type }
    }

    // Busca la primera plaza libre de ese tipo, la marca como ocupada y devuelve el número
    fun occupyFirstAvailableSpot(type: VehicleType): Int? {
        val spot = spots.find { it.zone == type && !it.isOccupied }
        spot?.let {
            it.isOccupied = true
            return it.number
        }
        return null // No hay plazas libres
    }

    // Libera una plaza por su número (para cuando se cancela la reserva)
    fun releaseSpot(spotNumber: Int) {
        spots.find { it.number == spotNumber }?.let {
            it.isOccupied = false
        }
    }

    fun syncWithReservations(allBookings: List<com.lksnext.ParkingMMartinez.model.Reservation>) {
        spots.forEach { it.isOccupied = false }

        allBookings.forEach { res ->
            val spot = spots.find { it.number == res.spotNumber }
            if (spot != null) {
                spot.isOccupied = true
            }
        }
    }

}