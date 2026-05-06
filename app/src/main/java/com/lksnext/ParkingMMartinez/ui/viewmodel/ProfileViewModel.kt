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
    private val _vehicles = mutableStateListOf<Vehicle>(
//        Vehicle(name = "My Car", plate = "1234 ABC", type = VehicleType.STANDARD, isAdapted = false),
//        Vehicle(name = "Vespa", plate = "5678 XYZ", type = VehicleType.MOTORCYCLE, isAdapted = false)
    )
    val vehicles: List<Vehicle> get() = _vehicles

    fun loadUserVehicles(context: android.content.Context) {
        val userId = com.lksnext.ParkingMMartinez.data.SessionManager(context).getActiveUserId()

        if (userId == null) return

        // 1. Cargamos los coches
        val vManager = com.lksnext.ParkingMMartinez.data.VehicleManager(context)
        _vehicles.clear()
        _vehicles.addAll(vManager.getVehicles(userId))

        // 2. BUSCAMOS LOS DATOS DEL USUARIO (Para que cambie el nombre en la UI)
        val uManager = com.lksnext.ParkingMMartinez.data.UserManager(context)
        val userMock = com.lksnext.ParkingMMartinez.data.UserMock

        // Buscamos en SharedPreferences o en el Mock
        val user = uManager.getAllUsers().find { it.id == userId }
            ?: userMock.users.find { it.id == userId }

        user?.let {
            userName = it.username
            userEmail = it.email
            userRole = "User" // MEJORA: Lo que venga de ajustes en un futuro
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

    fun addVehicle(context: android.content.Context) {
        val userId = com.lksnext.ParkingMMartinez.data.SessionManager(context).getActiveUserId()

        // Si el botón no hace nada, es porque entra aquí y hace el return
        if (userId == null) {
            android.util.Log.e("PROFILE_ERROR", "No puedo añadir coche sin ID de usuario")
            return
        }

        if (newVehicleName.isNotBlank() && newVehiclePlate.isNotBlank()) {
            val newVehicle = com.lksnext.ParkingMMartinez.model.Vehicle(
                name = newVehicleName,
                plate = newVehiclePlate,
                type = selectedVehicleType,
                isAdapted = if (selectedVehicleType == VehicleType.ADAPTED) true else false
            )

            com.lksnext.ParkingMMartinez.data.VehicleManager(context).addVehicle(userId, newVehicle)
            _vehicles.add(newVehicle)
            onCloseDialog()
        }
    }

    fun confirmDeleteVehicle(context: android.content.Context) {
        val userId = com.lksnext.ParkingMMartinez.data.SessionManager(context).getActiveUserId() ?: return
        vehicleToDelete?.let { vehicle ->
            _vehicles.remove(vehicle)
            com.lksnext.ParkingMMartinez.data.VehicleManager(context).deleteVehicle(userId, vehicle)
        }
        dismissDeleteDialog()
    }
}