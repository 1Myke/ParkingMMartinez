package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = ""
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = ""
    }

    fun login(onSuccess: () -> Unit) {
        isLoading = true

        //MEJORAS: Analizar que tdo este bien
        if (!email.contains("@")) {
            errorMessage = "Please enter a valid email"
        } else if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
        } else {
            // Simulamos éxito
            onSuccess()
        }

        isLoading = false
    }
}