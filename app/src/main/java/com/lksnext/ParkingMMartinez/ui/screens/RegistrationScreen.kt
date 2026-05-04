package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksClickableLabel
import com.lksnext.ParkingMMartinez.ui.components.LksPasswordField
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    /*
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("")}
    var pass by remember { mutableStateOf("")}
    var passRepeat by remember { mutableStateOf("")}
    var plate by remember { mutableStateOf("")}
    
    var vehicleType by remember { mutableStateOf("Car") }
    
    var errorMessage by remember { mutableStateOf("") }
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange
        )

        Spacer(modifier = Modifier.height(24.dp))

        LksTextField(
            value = viewModel.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = "Name",
            isError = false //Simplemente es su nombre, no podemos saber si esta bien o mal
        )

        LksTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            label = "Last name",
            isError = false //Simplemente es su apellido, no podemos saber si esta bien o mal
        )

        LksTextField(
            value = viewModel.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = "Username",
            isError = viewModel.errorMessage.contains("username", true) //No puede haber usernames repetidos
        )

        //Elegir el type
        Text(
            text = "Vehicle Type",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val vehicles = listOf("Car", "Motorcycle", "Electric Car", "Adapted Car")
            vehicles.forEach { type ->
                val isSelected = viewModel.vehicleType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onVehicleTypeChange(type) },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LksOrange.copy(alpha = 0.2f),
                        selectedLabelColor = LksOrange,
                        selectedLeadingIconColor = LksOrange
                    )
                )
            }
        }

        LksTextField(
            value = viewModel.plate,
            onValueChange = { viewModel.onPlateChange(it.uppercase()) }, // Forzamos mayúsculas en la matrícula
            label = "License Plate",
            isError = isPlateInvalid(viewModel.plate) //Da error, de momento lo he escrito asi, pero ayudame porfa
        )

        LksTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Email",
            isError = viewModel.errorMessage.contains("email", true)
        )

        LksPasswordField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = "Password",
            isError = viewModel.errorMessage.contains("password", true)
        )

        LksPasswordField(
            value = viewModel.passwordRepeat,
            onValueChange = { viewModel.onPasswordRepeatChange(it) },
            label = "Confirm password",
            isError = viewModel.errorMessage.contains("password", true)
        )

        //Comprobar que las dos passwords sean iguales y que tienen bien el regex que pedire

        if (viewModel.errorMessage.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LksButton(
            text = "SIGN UP",
            enabled = (viewModel.name.isNotEmpty() &&
                    viewModel.lastName.isNotEmpty() &&
                    viewModel.username.isNotEmpty() &&
                    viewModel.email.isNotEmpty() &&
                    viewModel.password.isNotEmpty() &&
                    viewModel.passwordRepeat.isNotEmpty() &&
                    viewModel.plate.isNotEmpty() &&
                    viewModel.vehicleType.isNotEmpty()
                    ),
            onClick = {
                viewModel.register { onRegisterSuccess() }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                textAlign = TextAlign.Center
            )
            LksClickableLabel(
                text = "Log In",
                onClick = {
                    onNavigateToLogin()
                }
            )
        }


    }
}

fun isPlateInvalid(plate: String): Boolean {
    if (plate.isEmpty()) return false // No marcamos error si está vacío al empezar
    val regex = Regex("^[0-9]{4}[A-Z]{3}$") // Ejemplo: 1234ABC
    return !regex.matches(plate) // Si NO coincide, devolvemos TRUE (es un error)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen(
        onRegisterSuccess = {},
        onNavigateToLogin = {}
    )
}