package com.example.lksparking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lksparking.ui.components.LksButton
import com.example.lksparking.ui.components.LksPasswordField
import com.example.lksparking.ui.components.LksTextField
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun RegistrationScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("")}
    var pass by remember { mutableStateOf("")}
    var passRepeat by remember { mutableStateOf("")}
    var plate by remember { mutableStateOf("")}
    // var vehicleType (Seleccionarlo en un combo box y que te de la opcion a elegir que tipo de coche es el que tienes, como se hacia en el diseño, eso sera otro componente que usaremos tambien en el perfil)

    var errorMessage by remember { mutableStateOf("") }

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
            value = name,
            onValueChange = { name = it; errorMessage = "" },
            label = "Name",
            isError = false //Simplemente es su nombre, no podemos saber si esta bien o mal
        )

        LksTextField(
            value = lastName,
            onValueChange = { lastName = it; errorMessage = "" },
            label = "Last name",
            isError = false //Simplemente es su apellido, no podemos saber si esta bien o mal
        )

        LksTextField(
            value = username,
            onValueChange = { username = it; errorMessage = "" },
            label = "Username",
            isError = errorMessage.contains("username", true) //No puede haber usernames repetidos
        )

        //Elegir el type

        LksTextField(
            value = plate,
            onValueChange = { plate = it.uppercase(); errorMessage = "" }, // Forzamos mayúsculas en la matrícula
            label = "License Plate",
            isError = isPlateInvalid(plate) //Da error, de momento lo he escrito asi, pero ayudame porfa
        )

        LksTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = "Email",
            isError = errorMessage.contains("email", true)
        )

        LksPasswordField(
            value = pass,
            onValueChange = { pass = it; errorMessage = "" },
            label = "Password",
            isError = errorMessage.contains("password", true)
        )

        LksPasswordField(
            value = passRepeat,
            onValueChange = { passRepeat = it; errorMessage = "" },
            label = "Password",
            isError = errorMessage.contains("password", true)
        )

        //Comprobar que las dos passwords sean iguales y que tienen bien el regex que pedire

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LksButton(
            text = "SIGN UP",
            enabled = (name.isNotEmpty() &&
                    lastName.isNotEmpty() &&
                    username.isNotEmpty() &&
                    email.isNotEmpty() &&
                    pass.isNotEmpty() &&
                    passRepeat.isNotEmpty() &&
                    plate.isNotEmpty()),
                    //vehicleType
            onClick = {
                // AQUÍ VALIDAMOS TODO ANTES DE "ENVIAR"
                when {
                    !email.contains("@") -> errorMessage = "Invalid email format"
                    pass != passRepeat -> errorMessage = "Passwords do not match"
                    pass.length < 6 -> errorMessage = "Password too short"
                    isPlateInvalid(plate) -> errorMessage = "Invalid license plate (1234ABC)"
                    else -> onRegisterSuccess()
                }
            }
        )

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log In", color = Color.Gray)
        }


    }
}

fun isPlateInvalid(plate: String): Boolean {
    if (plate.isEmpty()) return false // No marcamos error si está vacío al empezar
    val regex = Regex("^[0-9]{4}[A-Z]{3}$") // Ejemplo: 1234ABC
    return !regex.matches(plate) // Si NO coincide, devolvemos TRUE (es un error)
}