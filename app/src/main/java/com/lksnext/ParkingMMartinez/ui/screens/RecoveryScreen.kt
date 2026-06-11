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
import androidx.compose.ui.platform.testTag
import com.lksnext.ParkingMMartinez.ui.constants.TestTags

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
            text = stringResource(R.string.rec_btn_continue),
            enabled = viewModel.email.isNotEmpty(),
            modifier = Modifier.testTag(TestTags.RECOVERY_SUBMIT_BTN),
            onClick = {
                viewModel.validateAndSend(onSuccess = { onNavigateBack() })
            }
        )
    }
}