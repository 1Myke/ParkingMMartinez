package com.lksnext.ParkingMMartinez.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.cremaSuave

@Composable
fun ProfileHeaderSection(
    name: String,
    role: String,
    email: String,
    photoUrl: String?,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmapImage = remember(photoUrl) {
        if (!photoUrl.isNullOrEmpty()) {
            try {
                val cleanBase64 = photoUrl.substringAfter("base64,")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clickable { onImageClick() },
            shape = CircleShape,
            border = BorderStroke(4.dp, cremaSuave),
            color = Color.LightGray,
            shadowElevation = 4.dp
        ) {
            if (bitmapImage != null) {
                AsyncImage(
                    model = bitmapImage,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(30.dp),
                    tint = LksOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        Text(role, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LksOrange)
        Text(email, fontSize = 14.sp, color = Color.Gray)
    }
}