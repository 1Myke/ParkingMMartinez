package com.example.lksparking.ui.screens

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lksparking.ui.components.LksButton
import com.example.lksparking.ui.components.LksClickableLabel
import com.example.lksparking.ui.components.LksPasswordField
import com.example.lksparking.ui.components.LksTextField
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordRecovery: () -> Unit
){
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange
        )

        Spacer(modifier = Modifier.height(32.dp))

        LksTextField(
            value = email,
            onValueChange = { email = it },
            label = "Username",
            leadingIcon = Icons.Default.Email,
            // El text field se pondra en rojo si en el mensaje de error esta la palabra email o username
            isError = errorMessage.contains("email", ignoreCase = true) ||
                    errorMessage.contains("user", ignoreCase = true)
        )

        LksPasswordField(
            value = pass,
            onValueChange = { pass = it},
            label = "Password",
            isError = errorMessage.contains("password", ignoreCase = true),
            leadingIcon = Icons.Default.Key
        )

        if (errorMessage.isNotEmpty()){
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LksClickableLabel(
            text = "Forgot Password?",
            onClick = {
                onNavigateToPasswordRecovery()
            },
            modifier = Modifier
                .align(Alignment.Start) // <--- ESTO lo empuja a la izquierda
                .padding(start = 4.dp)
        )

        LksButton(
            text = "LOG IN",
            enabled = email.isNotEmpty() && pass.isNotEmpty(),
            onClick = {
                errorMessage = ""
                // MEJORAS: AQUI HAY QUE VALIDAR QUE ESTE EN LA BASE DE DATOS
                // AL MIRAR EN LA BASE DE DATOS COMPROBAMOS AVER TANTO SI ES EL USERNAME O EL GMAIL PORQUE SE PUEDE ENTRAR CON AMBOS
                // EN EL REGISTER HAY QUE HACER HASH
                // EN EL REGISTER HAY QUE HACER LA VALIDACION DEL REGEX ANTES DE MANDARLO A LA BASE DE DATOS
                if (!email.contains("@")) {
                    errorMessage = "Please enter a valid email"
                } else if (pass.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                } else {
                    onLoginSuccess()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        )

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = {},
        onNavigateToRegister = {},
        onNavigateToPasswordRecovery = {}
    )
}