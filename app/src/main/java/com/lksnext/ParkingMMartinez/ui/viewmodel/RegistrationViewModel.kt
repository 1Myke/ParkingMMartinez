package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.screens.isPlateInvalid

class RegistrationViewModel(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var name by mutableStateOf("")
    var lastName by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordRepeat by mutableStateOf("")
    var plate by mutableStateOf("")
    var vehicleType by mutableStateOf("Car")
    var errorCode by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    // Funciones de cambio (onNameChange, etc.) se mantienen igual, reseteando errorCode a null
    fun onUsernameChange(v: String) { username = v; errorCode = null }
    fun onEmailChange(v: String) { email = v; errorCode = null }
    fun onPasswordChange(v: String) { password = v; errorCode = null }
    fun onPasswordRepeatChange(v: String) { passwordRepeat = v; errorCode = null }
    fun onPlateChange(v: String) { plate = v; errorCode = null }
    fun onVehicleTypeChange(v: String) { vehicleType = v }

    fun register(onSuccess: () -> Unit) {
        isLoading = true
        when {
            !email.contains("@") -> errorCode = "error_invalid_email" //MEJORAS: AÑADIR REGEX
            password.length < 6 -> errorCode = "error_password_short"
            password != passwordRepeat -> errorCode = "error_password_mismatch"
            isPlateInvalid(plate) -> errorCode = "error_invalid_plate"
            userRepository.getAllUsers().any { it.username == username } -> errorCode = "error_user_exists"
            else -> {
                val newUser = User(name=name, lastName=lastName, username=username, email=email, pass=password)
                userRepository.registerUser(newUser)
                sessionManager.saveSession(true, newUser.id)

                val type = when(vehicleType) {
                    "Motorcycle" -> VehicleType.MOTORCYCLE
                    "Electric Car" -> VehicleType.ELECTRIC
                    "Adapted Car" -> VehicleType.ADAPTED
                    else -> VehicleType.STANDARD
                }
                vehicleRepository.addVehicle(newUser.id, Vehicle(name="My Car", plate=plate, type=type, isAdapted = false))
                onSuccess()
            }
        }
        isLoading = false
    }
}