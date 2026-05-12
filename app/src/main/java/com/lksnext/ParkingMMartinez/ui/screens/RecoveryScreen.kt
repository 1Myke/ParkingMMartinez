package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.RecoveryViewModel

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel,
    onNavigateBack: () -> Unit
){
    // Mapeo del código de error al recurso de string
    val errorMessage = when (viewModel.errorCode) {
        "error_invalid_email_recovery" -> stringResource(R.string.err_invalid_email)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.rec_title),
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LksTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = stringResource(R.string.login_email_label), // Reutilizamos si existe
            placeholder = stringResource(R.string.rec_placeholder),
            isError = viewModel.errorCode != null
        )

        if (errorMessage != null){
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LksButton(
            text = stringResource(R.string.rec_btn_continue),
            enabled = viewModel.email.isNotEmpty(),
            onClick = {
                viewModel.validateAndSend(onSuccess = { onNavigateBack() })
            }
        )
    }
}