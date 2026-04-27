package com.example.lksparking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lksparking.ui.theme.LksOrange

@Composable
fun ProfileHeaderSection(
    name: String,
    role: String,
    email: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            border = BorderStroke(4.dp, Color(0xFFFFF5E9)), // Un naranja muy clarito
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(30.dp),
                tint = LksOrange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1C1E))
        Text(role, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LksOrange)
        Text(email, fontSize = 14.sp, color = Color.Gray)
    }
}