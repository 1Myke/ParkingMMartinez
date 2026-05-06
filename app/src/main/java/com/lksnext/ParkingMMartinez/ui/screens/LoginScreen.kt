package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.lksnext.ParkingMMartinez.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordRecovery: () -> Unit
){
    /*var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }*/

    val context = androidx.compose.ui.platform.LocalContext.current

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
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Username",
            //leadingIcon = Icons.Default.Email,
            // El text field se pondra en rojo si en el mensaje de error esta la palabra email o username
            isError = viewModel.errorMessage.contains("email", ignoreCase = true) ||
                    viewModel.errorMessage.contains("user", ignoreCase = true)
        )

        LksPasswordField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = "Password",
            isError = viewModel.errorMessage.contains("password", ignoreCase = true)//,
            //leadingIcon = Icons.Default.Key
        )

        if (viewModel.errorMessage.isNotEmpty()){
            Text(
                text = viewModel.errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LksClickableLabel(
                text = "Forgot Password?",
                onClick = {
                    onNavigateToPasswordRecovery()
                }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Remember me", style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.Checkbox(
                    checked = viewModel.rememberMe,
                    onCheckedChange = { viewModel.onRememberMeChange(it) },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = LksOrange
                    )
                )
            }
        }


        Spacer(modifier = Modifier.padding(4.dp))

        LksButton(
            text = "LOG IN",
            enabled = !viewModel.isLoading && viewModel.email.isNotEmpty() && viewModel.password.isNotEmpty(),
            onClick = {
                viewModel.login(context) { shouldRemember ->
                    onLoginSuccess(shouldRemember)
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?",
                textAlign = TextAlign.Center
            )
            LksClickableLabel(
                text = "Register here!",
                onClick = {
                    onNavigateToRegister()
                }
            )
        }


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