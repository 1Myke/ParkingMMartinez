package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.screens.isPlateInvalid

class RegistrationViewModel: ViewModel() {

    var name by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordRepeat by mutableStateOf("")
        private set
    var plate by mutableStateOf("")
        private set
    var vehicleType by mutableStateOf("Car")
        private set
    var errorMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onNameChange(newValue: String) {
        name = newValue
        errorMessage = ""
    }

    fun onLastNameChange(newValue: String) {
        lastName = newValue
        errorMessage = ""
    }

    fun onUsernameChange(newValue: String) {
        username = newValue
        errorMessage = ""
    }

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = ""
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = ""
    }

    fun onPasswordRepeatChange(newValue: String) {
        passwordRepeat = newValue
        errorMessage = ""
    }

    fun onPlateChange(newValue: String) {
        plate = newValue
        errorMessage = ""
    }

    fun onVehicleTypeChange(newValue: String) {
        vehicleType = newValue
        errorMessage = ""
    }

    fun register(context: android.content.Context, onSuccess: () -> Unit) {
        isLoading = true

        val userManager = com.lksnext.ParkingMMartinez.data.UserManager(context)
        val sessionManager = com.lksnext.ParkingMMartinez.data.SessionManager(context)

        when {
            email.isBlank() || !email.contains("@") -> errorMessage = "Invalid email format"
            password != passwordRepeat -> errorMessage = "Passwords do not match"
            password.length < 6 -> errorMessage = "Password too short"
            isPlateInvalid(plate) -> errorMessage = "Invalid license plate (1234ABC)"
            else -> {
                val newUser = com.lksnext.ParkingMMartinez.model.User(
                    name = name,
                    lastName = lastName,
                    username = username,
                    email = email,
                    pass = password
                )

                userManager.registerUser(newUser)

                sessionManager.saveSession(true, newUser.id)

                val selectedType = when (vehicleType) {
                    "Motorcycle" -> com.lksnext.ParkingMMartinez.model.VehicleType.MOTORCYCLE
                    "Electric Car" -> com.lksnext.ParkingMMartinez.model.VehicleType.ELECTRIC
                    "Adapted Car" -> com.lksnext.ParkingMMartinez.model.VehicleType.ADAPTED
                    else -> com.lksnext.ParkingMMartinez.model.VehicleType.STANDARD
                }

                val firstVehicle = com.lksnext.ParkingMMartinez.model.Vehicle(
                    id = newUser.id, //MEJORAS: Cuando use firebase añadir un campo al vehiculo que sea ownerID para distinguir de quien es el vehiculo
                    name = "My Vehicle",
                    plate = plate,
                    type = selectedType,
                    isAdapted = false
                )
                com.lksnext.ParkingMMartinez.data.VehicleManager(context).addVehicle(newUser.id, firstVehicle)

                onSuccess()
            }
        }

        isLoading = false
    }
}