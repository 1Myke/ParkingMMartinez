package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(userId: String): List<Vehicle>
    suspend fun addVehicle(userId: String, vehicle: Vehicle)
    suspend fun deleteVehicle(userId: String, vehicle: Vehicle)
}