package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun TimeSlider(
    currentHours: Float,
    onHoursChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = currentHours,
        onValueChange = { newValue ->
            if (newValue >= 1f) {
                onHoursChange(newValue)
            }
        },
        valueRange = 0f..8f,
        steps = 7,
        modifier = modifier,
        colors = SliderDefaults.colors(thumbColor = LksOrange, activeTrackColor = LksOrange)
    )
}