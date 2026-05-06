package com.lksnext.ParkingMMartinez.model

data class ParkingSpot (
    val number: Int,
    val zone: VehicleType,
    var isOccupied: Boolean = false
)