package com.lksnext.ParkingMMartinez.data

import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.model.ParkingSpot
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import java.util.Calendar

object ParkingManager {

    val zones: List<ParkingZone>
        get() = listOf(
            ParkingZone(
                name = ZoneNames.DISABILITY,
                availableSpots = getAvailableSpotsCount(VehicleType.ADAPTED),
                totalSpots = getTotalSpotsCount(VehicleType.ADAPTED),
                iconRes = 0,
                color = Color(0xFF2D5AF0)
            ),
            ParkingZone(
                name = ZoneNames.EV,
                availableSpots = getAvailableSpotsCount(VehicleType.ELECTRIC),
                totalSpots = getTotalSpotsCount(VehicleType.ELECTRIC),
                iconRes = 0,
                color = Color(0xFF00C853)
            ),
            ParkingZone(
                name = ZoneNames.STANDARD,
                availableSpots = getAvailableSpotsCount(VehicleType.STANDARD),
                totalSpots = getTotalSpotsCount(VehicleType.STANDARD),
                iconRes = 0,
                color = Color(0xFF455A64)
            ),
            ParkingZone(
                name = ZoneNames.MOTORCYCLE,
                availableSpots = getAvailableSpotsCount(VehicleType.MOTORCYCLE),
                totalSpots = getTotalSpotsCount(VehicleType.MOTORCYCLE),
                iconRes = 0,
                color = Color(0xFFA66FB5)
            )
        )

    private val spots = mutableListOf<ParkingSpot>().apply {
        repeat(6) { add(ParkingSpot(it + 1, VehicleType.ADAPTED)) }
        repeat(4) { add(ParkingSpot(it + 7, VehicleType.ELECTRIC)) }
        repeat(24) { add(ParkingSpot(it + 11, VehicleType.STANDARD)) }
        repeat(16) { add(ParkingSpot(it + 35, VehicleType.MOTORCYCLE)) }
    }

    fun getAvailableSpotsCount(type: VehicleType): Int {
        return spots.count { it.zone == type && !it.isOccupied }
    }

    fun getTotalSpotsCount(type: VehicleType): Int {
        return spots.count { it.zone == type }
    }

    fun occupyFirstAvailableSpot(type: VehicleType): Int? {
        val spot = spots.find { it.zone == type && !it.isOccupied }
        spot?.let {
            it.isOccupied = true
            return it.number
        }
        return null
    }

    fun releaseSpot(spotNumber: Int) {
        spots.find { it.number == spotNumber }?.let {
            it.isOccupied = false
        }
    }

    fun syncWithReservations(allBookings: List<Reservation>) {
        spots.forEach { it.isOccupied = false }
        allBookings.forEach { res ->
            val spot = spots.find { it.number == res.spotNumber }
            spot?.isOccupied = true
        }
    }
    private fun isReservationOverlapping(
        res: Reservation,
        calSelected: Calendar,
        calRes: Calendar,
        slotStart: java.time.LocalTime,
        slotEnd: java.time.LocalTime
    ): Boolean {
        calRes.time = res.date
        val isSameDay = calSelected.get(Calendar.YEAR) == calRes.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calRes.get(Calendar.DAY_OF_YEAR)

        if (!isSameDay) return false

        // Pasamos todas las cosas a minutos puros desde las 00:00
        val startSlot = slotStart.hour * 60 + slotStart.minute
        var endSlot = slotEnd.hour * 60 + slotEnd.minute
        val startRes = res.startTime.hour * 60 + res.startTime.minute
        var endRes = res.endTime.hour * 60 + res.endTime.minute

        if (endSlot < startSlot) endSlot += 1440
        if (endRes < startRes) endRes += 1440

        return startSlot < endRes && endSlot > startRes
    }

    fun syncWithReservationsForTimeSlot(
        allBookings: List<Reservation>,
        selectedDate: java.util.Date,
        slotStart: java.time.LocalTime,
        slotEnd: java.time.LocalTime
    ) {
        spots.forEach { it.isOccupied = false }

        val cal1 = Calendar.getInstance().apply { time = selectedDate }
        val cal2 = Calendar.getInstance()

        val conflictCounts = mutableMapOf<VehicleType, Int>().withDefault { 0 }

        allBookings.forEach { res ->
            if (isReservationOverlapping(res, cal1, cal2, slotStart, slotEnd)) {
                val vehicleType = res.vehicle.type
                conflictCounts[vehicleType] = conflictCounts.getValue(vehicleType) + 1
            }
        }

        conflictCounts.forEach { (type, count) ->
            var occupiedSoFar = 0
            spots.forEach { spot ->
                if (spot.zone == type && occupiedSoFar < count) {
                    spot.isOccupied = true
                    occupiedSoFar++
                }
            }
        }
    }

    fun findFirstAvailableSpotNumber(
        allBookings: List<Reservation>,
        zoneName: String,
        vehicleType: VehicleType,
        selectedDate: java.util.Date,
        slotStart: java.time.LocalTime,
        slotEnd: java.time.LocalTime,
        editingReservationId: String? = null
    ): Int {
        val zoneSpots = spots.filter { it.zone == vehicleType }.map { it.number }

        val cal1 = Calendar.getInstance().apply { time = selectedDate }
        val cal2 = Calendar.getInstance()

        val occupiedSpotNumbers = mutableSetOf<Int>()

        allBookings.forEach { res ->
            if (editingReservationId != null && res.id == editingReservationId) {
                return@forEach
            }

            if (res.zone.name == zoneName && isReservationOverlapping(res, cal1, cal2, slotStart, slotEnd)) {
                occupiedSpotNumbers.add(res.spotNumber)
            }
        }

        return zoneSpots.find { it !in occupiedSpotNumbers } ?: zoneSpots.firstOrNull() ?: 0
    }
}