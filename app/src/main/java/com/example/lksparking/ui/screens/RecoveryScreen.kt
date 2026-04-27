package com.example.lksparking.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.example.lksparking.ui.components.LksTextField
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun RecoveryScreen(
    onRecoverySuccess: () -> Unit ,
    onNavigateToLogin: () -> Unit
){
    var email by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Recover your password",
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange
        )

        Spacer(modifier = Modifier.height(32.dp))

        LksTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "Use your registration email", //MEJORAS: Puedo hacer que cuando meta algo que no es el email, emplan el username se mande al email que hay asociado
            isError = errorMessage.contains("email", ignoreCase = true) ||
                        errorMessage.contains("user", ignoreCase = true)
        )

        if (errorMessage.isNotEmpty()){
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.padding(12.dp))

        LksButton(
            text = "Continue",
            enabled = email.isNotEmpty(),
            onClick = {
                errorMessage = ""
                // MEJORAS: AQUI HAY QUE VALIDAR QUE ESTE EN LA BASE DE DATOS
                // AL MIRAR EN LA BASE DE DATOS COMPROBAMOS AVER TANTO SI ES EL USERNAME O EL GMAIL PORQUE SE PUEDE ENTRAR CON AMBOS
                // EN EL REGISTER HAY QUE HACER HASH
                // EN EL REGISTER HAY QUE HACER LA VALIDACION DEL REGEX ANTES DE MANDARLO A LA BASE DE DATOS
                if (!email.contains("@")) {
                    errorMessage = "Please enter a valid email"
                } else {
                    onRecoverySuccess()
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecoveryScreenPreview() {
    RecoveryScreen(
        onRecoverySuccess = {},
        onNavigateToLogin = {}
    )
}