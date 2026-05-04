package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType

class ProfileViewModel : ViewModel() {

    // Datos del usuario (luego vendrán de Firebase/Repository)
    var userName by mutableStateOf("1Myke")
    var userRole by mutableStateOf("Senior Operations Manager")
    var userEmail by mutableStateOf("mikel@lksnext.com")

    // Lista de vehículos reactiva
    private val _vehicles = mutableStateListOf(
        Vehicle(name = "My Car", plate = "1234 ABC", type = VehicleType.STANDARD, isAdapted = false),
        Vehicle(name = "Vespa", plate = "5678 XYZ", type = VehicleType.MOTORCYCLE, isAdapted = false)
    )
    val vehicles: List<Vehicle> get() = _vehicles

    fun addVehicle(vehicle: Vehicle) {
        _vehicles.add(vehicle)
    }

    fun deleteVehicle(vehicle: Vehicle) {
        _vehicles.remove(vehicle)
    }
}