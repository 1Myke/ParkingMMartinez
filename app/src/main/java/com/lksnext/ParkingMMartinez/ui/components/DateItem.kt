package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun DateItem(day: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) LksOrange else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.size(width = 62.dp, height = 75.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 10.sp, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
            Text(day, fontSize = 20.sp, color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.ExtraBold)
        }
    }
}