package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.Vehicle

interface VehicleRepository {
    fun getVehicles(userId: String): List<Vehicle>
    fun addVehicle(userId: String, vehicle: Vehicle)
    fun deleteVehicle(userId: String, vehicle: Vehicle) //MEJORAS: Usar un id de vehiculo para evitar lo de las matriculas
}