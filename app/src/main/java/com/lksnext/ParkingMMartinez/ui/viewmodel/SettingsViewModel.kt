package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmNewPassword by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var successCode by mutableStateOf<String?>(null)
    var errorCode by mutableStateOf<String?>(null)

    init {
        loadCurrentUserData()
    }

    private fun loadCurrentUserData() {
        val userId = sessionManager.getActiveUserId() ?: ""
        email = currentUser?.email ?: ""
        viewModelScope.launch {
            userRepository.getUserById(userId)?.let { user ->
                username = user.name
            }
        }
    }

    fun clearMessages() {
        errorCode = null
        successCode = null
    }

    fun updateProfile() {
        val userId = sessionManager.getActiveUserId() ?: ""
        if (username.isBlank()) {
            errorCode = "error_empty_field"
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                user?.let {
                    val updatedUser = it.copy(name = username)
                    userRepository.registerUser(updatedUser)
                    successCode = "success_profile_updated"
                }
            } catch (e: Exception) {
                errorCode = "error_updating_profile"
            } finally {
                isLoading = false
            }
        }
    }

    fun updatePassword() {
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
            errorCode = "error_empty_field"
            return
        }
        if (newPassword != confirmNewPassword) {
            errorCode = "error_passwords_do_not_match"
            return
        }
        if (newPassword.length < 6) {
            errorCode = "error_password_too_short"
            return
        }

        isLoading = true
        clearMessages()

        val credential = EmailAuthProvider.getCredential(currentUser?.email ?: "", oldPassword)

        currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            isLoading = false
                            if (updateTask.isSuccessful) {
                                successCode = "success_password_changed"
                                oldPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                            } else {
                                errorCode = "error_updating_password"
                            }
                        }
                } else {
                    isLoading = false
                    errorCode = "error_wrong_old_password"
                }
            }
    }
}