package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
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
import com.lksnext.ParkingMMartinez.ui.screens.isPlateInvalid
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.lksnext.ParkingMMartinez.data.repository.FirebaseUserRepository
import kotlinx.coroutines.tasks.await
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
    var userAvatar by mutableStateOf<String?>(null)

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
                    userAvatar = user.avatarURL
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

        if (isPlateInvalid(cleanPlate)) {
            vehicleAddError = R.string.err_invalid_plate // O como lo tengas en tu strings.xml (ej: R.string.err_invalid_plate)
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

        // Proceso de guardado asíncrono
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

        viewModelScope.launch {
            try {
                // Ahora esto es suspend, así que debe ir dentro de la corrutina
                val allBookings = bookingRepository.getAllReservations()
                val hasActiveBookings = allBookings.any { booking ->
                    booking.vehicle.plate == vehicle.plate
                }

                if (hasActiveBookings) {
                    vehicleDeleteError = R.string.error_active_booking
                    return@launch
                }

                vehicleRepository.deleteVehicle(userId, vehicle)
                _vehicles.remove(vehicle)
                dismissDeleteDialog()

            } catch (e: Exception) {
                vehicleDeleteError = R.string.error_generic
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        val userId = sessionManager.getActiveUserId() ?: return

        println("DEBUG_UPLOAD: Iniciando conversión a Base64 para evitar bloqueo de Storage...")

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Leer la URI y transformarla en un Bitmap comprimido
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                    println("DEBUG_UPLOAD_ERROR: No se pudo decodificar la imagen.")
                    return@launch
                }

                // Redimensionamos un poco la imagen para que no ocupe demasiado espacio en Firestore
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 300, 300, true)

                // 2. Comprimir el bitmap a formato JPEG en un flujo de bytes
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val imageBytes = outputStream.toByteArray()

                // 3. Convertir los bytes crudos a un String Base64 seguro
                val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                println("DEBUG_UPLOAD: Conversión exitosa. Tamaño del string: ${base64String.length} caracteres.")

                // 4. Guardar directamente la cadena Base64 en el campo 'avatarURL' de Firestore
                val success = userRepository.updateAvatar(userId, base64String)

                if (success) {
                    // Volvemos al hilo principal para actualizar la interfaz de Compose
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        userAvatar = base64String
                        println("DEBUG_UPLOAD: ¡Firestore actualizado con éxito usando Base64!")
                    }
                } else {
                    println("DEBUG_UPLOAD_ERROR: No se pudo actualizar el campo en Firestore.")
                }

            } catch (e: Exception) {
                println("DEBUG_UPLOAD_ERROR: Falló el procesamiento Base64 -> ${e.message}")
                e.printStackTrace()
            }
        }
    }

}