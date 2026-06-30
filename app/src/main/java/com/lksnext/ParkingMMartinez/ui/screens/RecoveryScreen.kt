package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.testTag
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel,
    onNavigateBack: () -> Unit
){
    var showSuccessDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearForm()
        }
    }

    val errorMessage = when (viewModel.errorCode) {
        "error_empty_field" -> stringResource(R.string.error_empty_fields)
        "error_invalid_email_recovery" -> stringResource(R.string.err_invalid_email)
        "error_email_not_found" -> stringResource(R.string.error_invalid_email_or_not_found)
        else -> null
    }

    // --- POPUP INFORMATIVO DE SPAM ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar tocando fuera */ },
            title = { Text(text = stringResource(R.string.rec_success_title)) },
            text = { Text(text = stringResource(R.string.rec_success_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(text = stringResource(R.string.btn_understood))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.lks_logo),
            contentDescription = "LKS Next Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = stringResource(R.string.rec_title),
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(TestTags.RECOVERY_TITLE)
        )

        Spacer(modifier = Modifier.height(32.dp))

        LksTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = stringResource(R.string.login_email_label),
            placeholder = stringResource(R.string.rec_placeholder),
            isError = viewModel.errorCode != null,
            modifier = Modifier.testTag(TestTags.RECOVERY_EMAIL_FIELD)
        )

        if (errorMessage != null){
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .testTag(TestTags.RECOVERY_ERROR_MSG)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LksButton(
            text = if (viewModel.isLoading) "..." else stringResource(R.string.rec_btn_continue),
            enabled = viewModel.email.isNotEmpty() && !viewModel.isLoading,
            modifier = Modifier.testTag(TestTags.RECOVERY_SUBMIT_BTN),
            onClick = {
                viewModel.validateAndSend(onSuccess = {
                    showSuccessDialog = true
                })
            }
        )
    }
}