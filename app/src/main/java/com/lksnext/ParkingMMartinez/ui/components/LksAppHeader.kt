package com.lksnext.ParkingMMartinez.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange

@Composable
fun LksAppHeader(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = LksOrange,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
           modifier = Modifier
               .fillMaxWidth()
               .height(80.dp)
               .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.lks_logo_sin_naranja),
                contentDescription = "Logo Empresa",
                modifier = Modifier
                    .height(70.dp)
                    .wrapContentWidth()
            )
        }
    }
}