package com.example.lksparking.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lksparking.ui.screens.isPlateInvalid

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

    fun register(onSuccess: () -> Unit) {
        isLoading = true

        when {
            email.isBlank() || !email.contains("@") -> errorMessage = "Invalid email format"
            password != passwordRepeat -> errorMessage = "Passwords do not match"
            password.length < 6 -> errorMessage = "Password too short"
            isPlateInvalid(plate) -> errorMessage = "Invalid license plate (1234ABC)"
            else -> {
                // Aquí mañana crearemos el usuario en Firebase
                onSuccess()
            }
        }

        isLoading = false
    }
}