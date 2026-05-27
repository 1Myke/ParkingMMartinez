package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Suppress("kotlin:S107") //MEJORAS: No estoy yo seguro de si es lo mas adecuado, pero no se me ocurria otra cosa para quitar el warning de sonar
@Composable
fun LksTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isError,
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null)}
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LksOrange,
            focusedLabelColor = LksOrange,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        ),
        singleLine = true,
        maxLines = 1
    )
}