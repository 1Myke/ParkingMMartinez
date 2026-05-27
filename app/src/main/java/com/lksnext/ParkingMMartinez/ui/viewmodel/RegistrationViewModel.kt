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
import com.lksnext.ParkingMMartinez.R // Importamos tus recursos

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
    var selectedVehicleType by mutableStateOf(VehicleType.STANDARD)
    var errorCode by mutableStateOf<Int?>(null)
        private set

    var isLoading by mutableStateOf(false)

    fun onUsernameChange(v: String) { username = v; errorCode = null }
    fun onEmailChange(v: String) { email = v; errorCode = null }
    fun onPasswordChange(v: String) { password = v; errorCode = null }
    fun onPasswordRepeatChange(v: String) { passwordRepeat = v; errorCode = null }
    fun onPlateChange(v: String) { plate = v; errorCode = null }

    fun onVehicleTypeChange(type: VehicleType) { selectedVehicleType = type }

    fun register(onSuccess: () -> Unit) {
        isLoading = true

        val cleanPlate = plate.replace("\\s".toRegex(), "").uppercase()

        when {
            !email.contains("@") -> errorCode = R.string.err_invalid_email
            password.length < 6 -> errorCode = R.string.err_password_short
            password != passwordRepeat -> errorCode = R.string.err_password_mismatch
            isPlateInvalid(cleanPlate) -> errorCode = R.string.err_invalid_plate
            userRepository.getAllUsers().any { it.username == username } -> errorCode = R.string.err_user_exists
            else -> {
                val newUser = User(name=name, lastName=lastName, username=username, email=email, pass=password)
                userRepository.registerUser(newUser)
                sessionManager.saveSession(true, newUser.id)

                val defaultVehicle = Vehicle(
                    userId = newUser.id,
                    name = "My Vehicle",
                    plate = cleanPlate,
                    type = selectedVehicleType
                )

                vehicleRepository.addVehicle(newUser.id, defaultVehicle)
                onSuccess()
            }
        }
        isLoading = false
    }
}