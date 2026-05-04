package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RecoveryViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf("")
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = "" // Limpiamos el error al escribir
    }

    fun validateAndSend(onSuccess: () -> Unit) {
        if (!email.contains("@") && !email.contains(".")) {
            errorMessage = "Please enter a valid email or username"
        } else {
            // Aquí irá la lógica de Firebase después
            onSuccess()
        }
    }
}