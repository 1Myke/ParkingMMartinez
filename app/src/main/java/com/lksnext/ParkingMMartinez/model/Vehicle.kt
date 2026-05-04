package com.lksnext.ParkingMMartinez.model

import java.util.UUID

enum class VehicleType{
    STANDARD,
    ELECTRIC,
    MOTORCYCLE
}

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val plate: String,
    val type: VehicleType,
    val isAdapted: Boolean
)