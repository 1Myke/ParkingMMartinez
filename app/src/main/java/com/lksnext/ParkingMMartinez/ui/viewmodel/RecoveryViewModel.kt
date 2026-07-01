package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RecoveryViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set

    var errorCode by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    fun onEmailChange(newValue: String) {
        email = newValue.trim()
        errorCode = null
    }

    fun validateAndSend(onSuccess: () -> Unit) {
        if (email.isBlank()) {
            errorCode = "error_empty_field"
            return
        }

        if (!email.matches(emailRegex)) {
            errorCode = "error_invalid_email_recovery"
            return
        }

        isLoading = true
        errorCode = null

        // Petición nativa a Firebase para enviar el correo de recuperación
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val exception = task.exception
                    android.util.Log.e("FIREBASE_AUTH", "Error recovery: ${exception?.message}")

                    errorCode = "error_email_not_found"
                }
            }
    }

    fun clearForm() {
        email = ""
        errorCode = null
        isLoading = false
    }
}