package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
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
    var vehicleErrorMessage by mutableStateOf<Int?>(null)
        private set


    // Lista de vehículos reactiva
    private val _vehicles = mutableStateListOf<Vehicle>()
    val vehicles: List<Vehicle> get() = _vehicles
    var vehicleAddError by mutableStateOf<Int?>(null)
        private set

    var vehicleDeleteError by mutableStateOf<Int?>(null)
        private set

    fun loadUserData() {
        val userId = sessionManager.getActiveUserId()

        if (userId == null) {
            println("DEBUG: Error, no hay ID de usuario en sesión.")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Cargar usuario de Firestore a través del repo
                val user = userRepository.getUserById(userId)

                if (user != null) {
                    println("DEBUG: Datos recibidos: ${user.name}, ${user.username}")
                    userName = user.username // O user.name si prefieres el nombre real
                    userEmail = user.email
                    userRole = "LKS Next Member"
                } else {
                    println("DEBUG: Usuario con ID $userId no encontrado en Firestore.")
                }

                // 2. Cargar vehículos
                val userVehicles = vehicleRepository.getVehicles(userId)
                _vehicles.clear()
                _vehicles.addAll(userVehicles)

            } catch (e: Exception) {
                println("DEBUG: Error al cargar datos: ${e.message}")
            }
        }
    }

    fun onOpenDialog() {
        vehicleAddError = null
        showAddVehicleDialog = true
    }

    fun onCloseDialog() {
        newVehicleName = ""
        newVehiclePlate = ""
        vehicleAddError = null
        showAddVehicleDialog = false
    }

    fun askDeleteVehicle(vehicle: Vehicle) {
        vehicleDeleteError = null
        vehicleToDelete = vehicle
        showDeleteConfirmation = true
    }

    fun dismissDeleteDialog() {
        showDeleteConfirmation = false
        vehicleToDelete = null
        vehicleDeleteError = null
    }

    fun onVehicleTypeChange(type: VehicleType) {
        selectedVehicleType = type
    }

    fun addVehicle() {
        val userId = sessionManager.getActiveUserId() ?: return
        vehicleAddError = null

        val cleanPlate = newVehiclePlate.replace("\\s".toRegex(), "").uppercase()
        val cleanName = newVehicleName.trim()

        if (cleanName.isBlank() || cleanPlate.isBlank()) {
            vehicleAddError = R.string.error_empty_fields
            return
        }

        val isDuplicate = _vehicles.any { it.plate == cleanPlate }
        if (isDuplicate) {
            vehicleAddError = R.string.error_duplicate_plate
            return
        }

        val newVehicle = Vehicle(
            userId = userId,
            name = cleanName,
            plate = cleanPlate,
            type = selectedVehicleType
        )

        // CORRECCIÓN: Ahora es una función suspend, debe ir en un launch
        viewModelScope.launch {
            try {
                vehicleRepository.addVehicle(userId, newVehicle)
                _vehicles.add(newVehicle)
                onCloseDialog()
            } catch (e: Exception) {
                vehicleAddError = R.string.error_generic
            }
        }
    }

    fun confirmDeleteVehicle() {
        val userId = sessionManager.getActiveUserId() ?: return
        val vehicle = vehicleToDelete ?: return

        val hasActiveBookings = bookingRepository.getAllReservations().any { booking ->
            booking.vehicle.plate == vehicle.plate
        }

        if (hasActiveBookings) {
            vehicleDeleteError = R.string.error_active_booking
            return
        }

        viewModelScope.launch {
            try {
                vehicleRepository.deleteVehicle(userId, vehicle)
                _vehicles.remove(vehicle)
                dismissDeleteDialog()
            } catch (e: Exception) {
                vehicleDeleteError = R.string.error_generic
            }
        }
    }
}