package com.example.lksparking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun LksHeader(title: String, subtitle: String){
    // El lienzo sobre el que vamos a dibujar todo
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LksOrange
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            // Espacio entre el titulo y el subtitulo
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}