package com.example.lksparking.model

enum class VehicleType{
    STANDARD,
    ELECTRIC,
    MOTORCYCLE
}

data class Vehicle(
    val name: String,
    val plate: String,
    val type: VehicleType,
    val isAdapted: Boolean
)