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

    var showAddVehicleDialog by mutableStateOf(false)
        private set
    var showDeleteConfirmation by mutableStateOf(false)
        private set
    var vehicleToDelete by mutableStateOf<Vehicle?>(null)
        private set

    var newVehicleName by mutableStateOf("")
    var newVehiclePlate by mutableStateOf("")
    var selectedVehicleType by mutableStateOf(VehicleType.STANDARD)


    // Lista de vehículos reactiva
    private val _vehicles = mutableStateListOf(
        Vehicle(name = "My Car", plate = "1234 ABC", type = VehicleType.STANDARD, isAdapted = false),
        Vehicle(name = "Vespa", plate = "5678 XYZ", type = VehicleType.MOTORCYCLE, isAdapted = false)
    )
    val vehicles: List<Vehicle> get() = _vehicles

    fun onOpenDialog() {
        showAddVehicleDialog = true
    }

    fun onCloseDialog() {
        newVehicleName = ""
        newVehiclePlate = ""
        showAddVehicleDialog = false
    }

    fun askDeleteVehicle(vehicle: Vehicle) {
        vehicleToDelete = vehicle
        showDeleteConfirmation = true
    }

    fun dismissDeleteDialog() {
        showDeleteConfirmation = false
        vehicleToDelete = null
    }

    fun onVehicleTypeChange(type: VehicleType) {
        selectedVehicleType = type
    }

    fun addVehicle() {
        if (newVehicleName.isNotBlank() && newVehiclePlate.isNotBlank()){
            _vehicles.add(
                Vehicle(
                    name = newVehicleName,
                    plate = newVehiclePlate,
                    type = selectedVehicleType,
                    isAdapted = false
                )
            )
            onCloseDialog()
        }
    }

    fun confirmDeleteVehicle() {
        vehicleToDelete?.let {
            _vehicles.remove(it)
        }
        dismissDeleteDialog()
    }
}