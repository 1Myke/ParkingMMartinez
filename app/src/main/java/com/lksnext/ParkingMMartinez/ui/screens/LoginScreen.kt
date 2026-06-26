package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksClickableLabel
import com.lksnext.ParkingMMartinez.ui.components.LksPasswordField
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.LoginViewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.LanguageSelectorSection
import com.lksnext.ParkingMMartinez.ui.constants.TestTags

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordRecovery: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val errorMessage = when (viewModel.errorCode) {
        "error_invalid_credentials" -> stringResource(R.string.err_login_failed)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(start = 24.dp, top = 24.dp, end = 24.dp)
        ) {
            LanguageSelectorSection()
        }

        // El formulario centrado con su padding original
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.headlineLarge,
                color = LksOrange,
                modifier = Modifier.testTag(TestTags.LOGIN_TITLE)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LksTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = stringResource(R.string.login_username),
                isError = viewModel.errorCode != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.testTag(TestTags.LOGIN_EMAIL_FIELD)
            )

            LksPasswordField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = stringResource(R.string.login_password),
                isError = viewModel.errorCode != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.testTag(TestTags.LOGIN_PASSWORD_FIELD)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .testTag(TestTags.LOGIN_ERROR_MSG)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LksClickableLabel(
                    text = stringResource(R.string.login_forgot),
                    onClick = {
                        focusManager.clearFocus()
                        onNavigateToPasswordRecovery()
                    },
                    modifier = Modifier.testTag(TestTags.LOGIN_FORGOT_PASSWORD_LINK)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.login_remember),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Checkbox(
                        checked = viewModel.rememberMe,
                        onCheckedChange = { viewModel.onRememberMeChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = LksOrange),
                        modifier = Modifier.testTag(TestTags.LOGIN_REMEMBER_ME_CHECKBOX)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            LksButton(
                text = stringResource(R.string.login_btn),
                enabled = !viewModel.isLoading && viewModel.email.isNotEmpty() && viewModel.password.isNotEmpty(),
                modifier = Modifier.testTag(TestTags.LOGIN_SUBMIT_BTN),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login { shouldRemember ->
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
                    text = stringResource(R.string.login_no_account),
                    textAlign = TextAlign.Center
                )
                LksClickableLabel(
                    text = stringResource(R.string.login_register_link),
                    onClick = {
                        focusManager.clearFocus()
                        onNavigateToRegister()
                    },
                    modifier = Modifier.testTag(TestTags.LOGIN_REGISTER_LINK)
                )
            }
        }
    }
}