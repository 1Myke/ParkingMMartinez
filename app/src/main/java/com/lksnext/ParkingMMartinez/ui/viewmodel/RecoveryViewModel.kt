package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RecoveryViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var errorCode by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        errorCode = null
    }

    fun validateAndSend(onSuccess: () -> Unit) {
        // MEJORAS: PONER UN BUEN REGEX
        if (!email.contains("@")) {
            errorCode = "error_invalid_email_recovery"
        } else {
            // Aquí irá la lógica de Firebase para enviar el correo de reset
            onSuccess()
        }
    }
}