package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.LanguageSelectorSection
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksPasswordField
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserData()
    }

    val feedbackMessage = when (viewModel.errorCode) {
        "error_empty_field" -> stringResource(R.string.error_empty_fields)
        "error_passwords_do_not_match" -> stringResource(R.string.err_password_mismatch)
        "error_password_too_short" -> stringResource(R.string.error_password_too_short)
        "error_wrong_old_password" -> stringResource(R.string.error_wrong_old_password)
        "error_updating_profile", "error_updating_password" -> stringResource(R.string.error_generic)
        else -> null
    }

    val successMessage = when (viewModel.successCode) {
        "success_profile_updated" -> stringResource(R.string.success_profile_updated)
        "success_password_changed" -> stringResource(R.string.success_password_changed)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LksOrange
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LksOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        // Seleccion de Idioma
        Text(text = stringResource(R.string.settings_language_section), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LanguageSelectorSection(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // Perfil del Usuario
        Text(text = stringResource(R.string.settings_profile_section), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${stringResource(R.string.login_email_label)}: ${viewModel.email}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))
        LksTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it; viewModel.clearMessages() },
            label = stringResource(R.string.settings_label_username),
            placeholder = "Nombre"
        )
        Spacer(modifier = Modifier.height(12.dp))
        LksButton(
            text = stringResource(R.string.settings_btn_save_profile),
            onClick = { viewModel.updateProfile() },
            enabled = !viewModel.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // Cambiar Contraseña
        Text(text = stringResource(R.string.settings_password_section), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        LksPasswordField(
            value = viewModel.oldPassword,
            onValueChange = { viewModel.oldPassword = it; viewModel.clearMessages() },
            label = stringResource(R.string.settings_label_old_password)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LksPasswordField(
            value = viewModel.newPassword,
            onValueChange = { viewModel.newPassword = it; viewModel.clearMessages() },
            label = stringResource(R.string.settings_label_new_password)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LksPasswordField(
            value = viewModel.confirmNewPassword,
            onValueChange = { viewModel.confirmNewPassword = it; viewModel.clearMessages() },
            label = stringResource(R.string.reg_label_confirm_password)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LksButton(
            text = stringResource(R.string.settings_btn_change_password),
            onClick = { viewModel.updatePassword() },
            enabled = !viewModel.isLoading
        )

        feedbackMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }
        successMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Green, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}