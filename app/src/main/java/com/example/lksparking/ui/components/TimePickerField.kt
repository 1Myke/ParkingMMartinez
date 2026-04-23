package com.example.lksparking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimePickerField(
    startTime: String,
    onOpenPicker: () -> Unit
) {
    OutlinedCard(
        onClick = onOpenPicker,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = startTime, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Menu, contentDescription = null)
        }
    }
}