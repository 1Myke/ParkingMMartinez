package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.RecoveryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel = viewModel(),
    onNavigateBack: () -> Unit
){
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
            color = LksOrange,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LksTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Email",
            placeholder = "Use your registration email", //MEJORAS: Puedo hacer que cuando meta algo que no es el email, emplan el username se mande al email que hay asociado
            isError = viewModel.errorMessage.contains("email", ignoreCase = true) ||
                        viewModel.errorMessage.contains("user", ignoreCase = true)
        )

        if (viewModel.errorMessage.isNotEmpty()){
            Text(
                text = viewModel.errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.padding(12.dp))

        LksButton(
            text = "Continue",
            enabled = viewModel.email.isNotEmpty(),
            onClick = {
                viewModel.validateAndSend(onSuccess = { onNavigateBack() })
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecoveryScreenPreview() {
    RecoveryScreen(
        onNavigateBack = {}
    )
}