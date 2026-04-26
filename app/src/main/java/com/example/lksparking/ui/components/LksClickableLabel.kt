package com.example.lksparking.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun LksClickableLabel(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LksOrange
){
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            textDecoration = TextDecoration.Underline
        )
    }
}