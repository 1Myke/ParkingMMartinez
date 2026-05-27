package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.VehicleManager
import com.lksnext.ParkingMMartinez.model.Vehicle

class LocalVehicleRepository(context: Context) : VehicleRepository {
    private val manager = VehicleManager(context)
    override fun getVehicles(userId: String) = manager.getVehicles(userId)
    override fun addVehicle(userId: String, vehicle: Vehicle) = manager.addVehicle(userId, vehicle)
    override fun deleteVehicle(userId: String, vehicle: Vehicle) = manager.deleteVehicle(userId, vehicle)
}