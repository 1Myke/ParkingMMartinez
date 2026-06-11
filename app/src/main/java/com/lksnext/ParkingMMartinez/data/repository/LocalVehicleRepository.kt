package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.VehicleManager
import com.lksnext.ParkingMMartinez.model.Vehicle

class LocalVehicleRepository(context: Context) : VehicleRepository {
    private val manager = VehicleManager(context)
    override suspend fun getVehicles(userId: String) = manager.getVehicles(userId)
    override suspend fun addVehicle(userId: String, vehicle: Vehicle) = manager.addVehicle(userId, vehicle)
    override suspend fun deleteVehicle(userId: String, vehicle: Vehicle) = manager.deleteVehicle(userId, vehicle)
}