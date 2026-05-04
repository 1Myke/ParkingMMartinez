package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun TimeSlider(
    currentHours: Float,
    onHoursChange: (Float) -> Unit
) {
    Slider(
        value = currentHours,
        onValueChange = onHoursChange,
        valueRange = 1f..8f,
        steps = 7,
        colors = SliderDefaults.colors(thumbColor = LksOrange, activeTrackColor = LksOrange)
    )
}