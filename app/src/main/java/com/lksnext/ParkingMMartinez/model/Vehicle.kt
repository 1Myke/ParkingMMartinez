package com.lksnext.ParkingMMartinez.model

import java.util.UUID

enum class VehicleType{
    STANDARD,
    ELECTRIC,
    MOTORCYCLE,
    ADAPTED
}

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val plate: String = "",
    val type: VehicleType = VehicleType.STANDARD,
)