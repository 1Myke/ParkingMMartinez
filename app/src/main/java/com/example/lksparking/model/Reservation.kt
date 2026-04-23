package com.example.lksparking.model

import java.time.LocalTime
import java.util.UUID
import java.util.Date

data class Reservation(
    val id: String = UUID.randomUUID().toString(),
    val vehicle: Vehicle,
    val zone: ParkingZone,
    val date: Date,
    val startTime: LocalTime, //Tiene metodos como Duration.between().toMinutes()....
    val endTime: LocalTime,
    val isCheckedIn: Boolean
)