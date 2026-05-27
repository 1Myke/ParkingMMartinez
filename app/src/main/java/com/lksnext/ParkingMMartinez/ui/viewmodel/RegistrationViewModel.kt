package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.screens.isPlateInvalid
import com.lksnext.ParkingMMartinez.R // Importamos tus recursos
import kotlinx.coroutines.launch

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

        viewModelScope.launch {
            when {
                !email.contains("@") -> { errorCode = R.string.err_invalid_email; isLoading = false }
                password.length < 6 -> { errorCode = R.string.err_password_short; isLoading = false }
                password != passwordRepeat -> { errorCode = R.string.err_password_mismatch; isLoading = false }
                isPlateInvalid(cleanPlate) -> { errorCode = R.string.err_invalid_plate; isLoading = false }
                // MEJORAS FIREBASE: Firebase no permite listar todos los usuarios, así que esta validación
                // de 'username exists' la omitiremos de momento o la haremos en Firestore luego.
                else -> {
                    val newUser = User(name=name, lastName=lastName, username=username, email=email, pass=password)

                    // LLAMADA A FIREBASE
                    val success = userRepository.registerUser(newUser)

                    if (success) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val realId = currentUser?.uid ?: newUser.id

                        sessionManager.saveSession(true, realId)


                        // ... (resto del registro de vehículo)
                        isLoading = false
                        onSuccess()
                    } else {
                        errorCode = R.string.err_registration_failed // Crea este string si no existe
                        isLoading = false
                    }
                }
            }
        }
    }
}