package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorCode by mutableStateOf<String?>(null) // Guardamos el ID del error
    var isLoading by mutableStateOf(false)
    var rememberMe by mutableStateOf(false)

    fun onEmailChange(newValue: String) { email = newValue; errorCode = null }
    fun onPasswordChange(newValue: String) { password = newValue; errorCode = null }
    fun onRememberMeChange(newValue: Boolean) { rememberMe = newValue }

    fun login(onSuccess: (Boolean) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            val user = userRepository.authenticate(email, password)

            if (user != null) {
                sessionManager.saveSession(rememberMe, user.id)
                onSuccess(rememberMe)
            } else {
                errorCode = "error_invalid_credentials"
            }
            isLoading = false
        }
    }

    fun resetLoginFields() {
        email = ""
        password = ""
        errorCode = null
    }
}