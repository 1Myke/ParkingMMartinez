package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType

class ProfileViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Datos del usuario (luego vendrán de Firebase/Repository)
    var userName by mutableStateOf("")
    var userRole by mutableStateOf("")
    var userEmail by mutableStateOf("")

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
    private val _vehicles = mutableStateListOf<Vehicle>()
    val vehicles: List<Vehicle> get() = _vehicles

    fun loadUserData() {
        val userId = sessionManager.getActiveUserId() ?: return

        val userVehicles = vehicleRepository.getVehicles(userId)
        _vehicles.clear()
        _vehicles.addAll(userVehicles)


        val user = userRepository.getUserById(userId)
        user?.let {
            userName = it.username
            userEmail = it.email
            userRole = "LKS Next Member" // MEJORAS: AÑADIR AJUSTES PARA PODER AÑADIR EL ROL ESPECIFICO
        }
    }



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
        val userId = sessionManager.getActiveUserId() ?: return

        if (newVehicleName.isNotBlank() && newVehiclePlate.isNotBlank()) {
            val newVehicle = Vehicle(
                name = newVehicleName,
                plate = newVehiclePlate,
                type = selectedVehicleType,
                isAdapted = selectedVehicleType == VehicleType.ADAPTED
            )

            vehicleRepository.addVehicle(userId, newVehicle)
            _vehicles.add(newVehicle)
            onCloseDialog()
        }
    }

    fun confirmDeleteVehicle() {
        val userId = sessionManager.getActiveUserId() ?: return
        vehicleToDelete?.let { vehicle ->
            vehicleRepository.deleteVehicle(userId, vehicle)
            _vehicles.remove(vehicle)
        }
        dismissDeleteDialog()
    }
}