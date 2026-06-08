package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.mistGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LksTimePicker(
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonModifier: Modifier = Modifier,
    dismissButtonModifier: Modifier = Modifier
) {
    // Definimos el estado inicial
    val state = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }, modifier = confirmButtonModifier) {
                Text(stringResource(R.string.timepick_confirm), color = LksOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = dismissButtonModifier) {
                Text(stringResource(R.string.timepick_cancel), color = Color.Gray)
            }
        },
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.timepick_selecttime),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 20.dp),
                    color = Color.Gray
                )

                // Usamos TimePicker pero con una configuración que
                // priorice la selección visual si es posible
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = mistGray,
                        selectorColor = LksOrange,
                        containerColor = Color.White,
                        periodSelectorSelectedContainerColor = LksOrange.copy(alpha = 0.2f),
                        timeSelectorSelectedContainerColor = LksOrange.copy(alpha = 0.1f),
                        timeSelectorSelectedContentColor = LksOrange
                    )
                )
            }
        }
    )
}