package com.example.lksparking.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import com.example.lksparking.ui.theme.LksOrange

@Composable
fun TimeSlider(
    currentHours: Float,
    onHoursChange: (Float) -> Unit
) {
    Slider(
        value = currentHours,
        onValueChange = onHoursChange,
        valueRange = 1f..9f,
        steps = 7,
        colors = SliderDefaults.colors(thumbColor = LksOrange, activeTrackColor = LksOrange)
    )
}