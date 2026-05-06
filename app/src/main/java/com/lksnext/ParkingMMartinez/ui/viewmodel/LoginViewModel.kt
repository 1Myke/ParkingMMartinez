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

    var rememberMe by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = ""
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = ""
    }

    fun login(context: android.content.Context, onSuccess: (Boolean) -> Unit) {
        isLoading = true

        val userManager = com.lksnext.ParkingMMartinez.data.UserManager(context)
        val sessionManager = com.lksnext.ParkingMMartinez.data.SessionManager(context)
        val userMock = com.lksnext.ParkingMMartinez.data.UserMock

        var user = userManager.authenticate(email, password)

        //MEJORAS: Analizar que tdo este bien
//        if (!email.contains("@")) {
//            errorMessage = "Please enter a valid email"
//        } else if (password.length < 6) {
//            errorMessage = "Password must be at least 6 characters"
//        } else {
//            // Simulamos éxito
//            onSuccess(rememberMe)
//        }
        if (user == null) {
            user = userMock.users.find { it.email == email && it.pass == password }
        }

        if (user != null) {
            android.util.Log.d("DEBUG_LOGIN", "Usuario encontrado: ${user.email}, ID: ${user.id}")
            sessionManager.saveSession(true, user.id)

            onSuccess(rememberMe)
        } else {
            errorMessage = "Invalid email or password"
        }
        isLoading = false
    }

    fun onRememberMeChange(newValue: Boolean) {
        rememberMe = newValue
    }
}