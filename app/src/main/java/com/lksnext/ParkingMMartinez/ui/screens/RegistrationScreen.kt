package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.components.*
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val errorMessage = viewModel.errorCode?.let { stringResource(id = it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.reg_title),
            style = MaterialTheme.typography.headlineLarge,
            color = LksOrange
        )

        Spacer(modifier = Modifier.height(24.dp))

        LksTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = stringResource(R.string.reg_label_name)
        )

        LksTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.lastName = it },
            label = "Last Name"
        )

        LksTextField(
            value = viewModel.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = stringResource(R.string.login_username),
            isError = viewModel.errorCode == R.string.err_user_exists
        )

        Text(
            text = stringResource(R.string.reg_vehicle_type),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehicleType.values().forEach { type ->
                val isSelected = viewModel.selectedVehicleType == type

                val typeLabel = when (type) {
                    VehicleType.STANDARD -> "Standard Car"
                    VehicleType.ELECTRIC -> "Electric Car"
                    VehicleType.MOTORCYCLE -> "Motorcycle"
                    VehicleType.ADAPTED -> "Adapted Car"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onVehicleTypeChange(type) },
                    label = { Text(typeLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LksOrange.copy(alpha = 0.2f),
                        selectedLabelColor = LksOrange
                    )
                )
            }
        }

        LksTextField(
            value = viewModel.plate,
            onValueChange = { viewModel.onPlateChange(it.uppercase()) },
            label = stringResource(R.string.reg_label_plate),
            isError = viewModel.errorCode == R.string.err_invalid_plate
        )

        LksTextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Email",
            isError = viewModel.errorCode == R.string.err_invalid_email
        )

        LksPasswordField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = stringResource(R.string.login_password),
            isError = viewModel.errorCode == R.string.err_password_short ||
                    viewModel.errorCode == R.string.err_password_mismatch
        )

        LksPasswordField(
            value = viewModel.passwordRepeat,
            onValueChange = { viewModel.onPasswordRepeatChange(it) },
            label = "Confirm Password",
            isError = viewModel.errorCode == R.string.err_password_mismatch
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LksButton(
            text = stringResource(R.string.reg_btn),
            enabled = !viewModel.isLoading && viewModel.name.isNotEmpty() && viewModel.email.isNotEmpty(),
            onClick = {
                focusManager.clearFocus()
                viewModel.register { onRegisterSuccess() }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Already have an account? ")
            LksClickableLabel(
                text = "Log In",
                onClick = onNavigateToLogin
            )
        }
    }
}

fun isPlateInvalid(plate: String): Boolean {
    if (plate.isEmpty()) return false
    val regex = Regex("^[0-9]{4}[A-Z]{3}$")
    return !regex.matches(plate)
}